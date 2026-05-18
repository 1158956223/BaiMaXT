package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.api.ApiResponse;
import com.example.client.TeacherClient;
import com.example.client.UserClient;
import com.example.domain.dto.LoginRequest;
import com.example.domain.dto.RegisterRequest;
import com.example.domain.po.AuthAccount;
import com.example.domain.vo.AuthResponse;
import com.example.domain.vo.CurrentUserResponse;
import com.example.dto.teacher.CreateTeacherProfileRequest;
import com.example.dto.teacher.TeacherProfileResponse;
import com.example.dto.user.CreateUserProfileRequest;
import com.example.dto.user.UserProfileResponse;
import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import com.example.exception.BusinessException;
import com.example.mapper.AuthAccountMapper;
import com.example.service.AuthService;
import com.example.tool.JwtTool;
import feign.FeignException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthAccountMapper authAccountMapper;
    private final UserClient userClient;
    private final TeacherClient teacherClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtTool jwtTool;

    public AuthServiceImpl(
            AuthAccountMapper authAccountMapper,
            UserClient userClient,
            TeacherClient teacherClient,
            PasswordEncoder passwordEncoder,
            JwtTool jwtTool
    ) {
        this.authAccountMapper = authAccountMapper;
        this.userClient = userClient;
        this.teacherClient = teacherClient;
        this.passwordEncoder = passwordEncoder;
        this.jwtTool = jwtTool;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        String username = requireText(request.username(), "Username must not be blank");
        String password = requireText(request.password(), "Password must not be blank");
        if (password.length() < 6) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Password length must be at least 6");
        }
        if (existsByUsername(username)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Username already exists");
        }
        String phone = trimToNull(request.phone());
        if (phone != null && existsByPhone(phone)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Phone already exists");
        }

        UserRole role = defaultRole(request.role());
        UserProfileResponse user = createUserProfile(request, username, role);
        createTeacherProfileIfNeeded(role, user, request);

        LocalDateTime now = LocalDateTime.now();
        AuthAccount account = new AuthAccount();
        account.setUserId(user.id());
        account.setUsername(username);
        account.setPhone(phone);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(role);
        account.setStatus(UserStatus.ENABLED);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        authAccountMapper.insert(account);
        return toAuthResponse(account, user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        String username = requireText(request.username(), "Username must not be blank");
        String password = requireText(request.password(), "Password must not be blank");
        AuthAccount account = findByUsername(username);
        if (account == null || !passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }
        if (UserStatus.DISABLED.equals(account.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Account is disabled");
        }
        UserProfileResponse user = getUserProfile(account.getUserId());
        return toAuthResponse(account, user);
    }

    @Override
    public CurrentUserResponse me(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        Map<String, Object> payload;
        try {
            payload = jwtTool.parseToken(token);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        Long accountId = jwtTool.getLong(payload, "accountId");
        Long userId = jwtTool.getLong(payload, "userId");
        String username = jwtTool.getString(payload, "username");
        UserRole role = parseRole(jwtTool.getString(payload, "role"));
        AuthAccount account = authAccountMapper.selectById(accountId);
        if (account == null || UserStatus.DISABLED.equals(account.getStatus())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (!account.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return new CurrentUserResponse(accountId, userId, username, role, getUserProfile(userId));
    }

    private UserProfileResponse createUserProfile(RegisterRequest request, String username, UserRole role) {
        try {
            ApiResponse<UserProfileResponse> response = userClient.createInternal(new CreateUserProfileRequest(
                    username,
                    defaultIfBlank(request.nickname(), username),
                    trimToNull(request.phone()),
                    trimToNull(request.email()),
                    role
            ));
            return unwrapUserResponse(response, "Failed to create user profile");
        } catch (FeignException.Conflict exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "Username already exists");
        }
    }

    private void createTeacherProfileIfNeeded(UserRole role, UserProfileResponse user, RegisterRequest request) {
        if (!UserRole.TEACHER.equals(role)) {
            return;
        }
        String specialties = requireText(request.teacherSpecialties(), "Teacher specialties must not be blank");
        try {
            ApiResponse<TeacherProfileResponse> response = teacherClient.createInternal(new CreateTeacherProfileRequest(
                    user.id(),
                    defaultIfBlank(user.nickname(), user.username()),
                    trimToNull(request.teacherTitle()),
                    trimToNull(request.teacherBio()),
                    specialties,
                    request.teacherYearsExperience()
            ));
            unwrapTeacherResponse(response, "Failed to create teacher profile");
        } catch (FeignException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to create teacher profile");
        }
    }

    private UserProfileResponse getUserProfile(Long userId) {
        try {
            return unwrapUserResponse(userClient.getInternal(userId), "Failed to get user profile");
        } catch (FeignException.NotFound exception) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User profile does not exist");
        }
    }

    private UserProfileResponse unwrapUserResponse(ApiResponse<UserProfileResponse> response, String message) {
        if (response == null || response.code() != 200 || response.data() == null) {
            String detail = response == null ? message : response.message();
            throw new BusinessException(HttpStatus.BAD_GATEWAY, detail);
        }
        return response.data();
    }

    private TeacherProfileResponse unwrapTeacherResponse(ApiResponse<TeacherProfileResponse> response, String message) {
        if (response == null || response.code() != 200 || response.data() == null) {
            String detail = response == null ? message : response.message();
            throw new BusinessException(HttpStatus.BAD_GATEWAY, detail);
        }
        return response.data();
    }

    private AuthResponse toAuthResponse(AuthAccount account, UserProfileResponse user) {
        String token = jwtTool.createToken(
                account.getId(),
                account.getUserId(),
                account.getUsername(),
                account.getRole()
        );
        return new AuthResponse(
                token,
                account.getId(),
                account.getUserId(),
                account.getUsername(),
                account.getRole(),
                user
        );
    }

    private boolean existsByUsername(String username) {
        return authAccountMapper.selectCount(new LambdaQueryWrapper<AuthAccount>()
                .eq(AuthAccount::getUsername, username)) > 0;
    }

    private boolean existsByPhone(String phone) {
        return authAccountMapper.selectCount(new LambdaQueryWrapper<AuthAccount>()
                .eq(AuthAccount::getPhone, phone)) > 0;
    }

    private AuthAccount findByUsername(String username) {
        return authAccountMapper.selectOne(new LambdaQueryWrapper<AuthAccount>()
                .eq(AuthAccount::getUsername, username)
                .last("limit 1"));
    }

    private String extractToken(String authorizationHeader) {
        String header = requireText(authorizationHeader, "Authorization header must not be blank");
        if (!header.startsWith("Bearer ")) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid authorization header");
        }
        return header.substring("Bearer ".length()).trim();
    }

    private UserRole defaultRole(UserRole role) {
        if (role == null) {
            return UserRole.STUDENT;
        }
        if (UserRole.ADMIN.equals(role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Admin accounts cannot be registered publicly");
        }
        return role;
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.valueOf(role);
        } catch (RuntimeException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private String requireText(String value, String message) {
        String text = trimToNull(value);
        if (text == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return text;
    }

    private String defaultIfBlank(String value, String fallback) {
        String text = trimToNull(value);
        return text == null ? fallback : text;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
