package com.example.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.dto.CreateUserRequest;
import com.example.domain.dto.LoginRequest;
import com.example.domain.dto.UpdateUserRequest;
import com.example.domain.dto.UserResponse;
import com.example.domain.po.User;
import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import com.example.exception.BusinessException;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import com.example.tool.PasswordTool;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordTool passwordTool;

    public UserServiceImpl(UserMapper userMapper, PasswordTool passwordTool) {
        this.userMapper = userMapper;
        this.passwordTool = passwordTool;
    }

    @Override
    public UserResponse create(CreateUserRequest request) {
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

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordTool.hash(password));
        user.setNickname(defaultIfBlank(request.nickname(), username));
        user.setPhone(trimToNull(request.phone()));
        user.setEmail(trimToNull(request.email()));
        user.setRole(defaultRole(request.role()));
        user.setStatus(UserStatus.ENABLED);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return toResponse(user);
    }

    @Override
    public UserResponse login(LoginRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        String username = requireText(request.username(), "Username must not be blank");
        String password = requireText(request.password(), "Password must not be blank");
        User user = findByUsername(username);
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "User is disabled");
        }
        if (!passwordTool.matches(password, user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }
        return toResponse(user);
    }

    @Override
    public List<UserResponse> listUser() {
        return userMapper.selectList(new LambdaQueryWrapper<User>().orderByAsc(User::getUid)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse get(Long id) {
        return toResponse(getUser(id));
    }

    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        User user = getUser(id);
        user.setNickname(defaultIfBlank(request.nickname(), user.getNickname()));
        user.setPhone(trimToNull(request.phone()));
        user.setEmail(trimToNull(request.email()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return toResponse(user);
    }

    @Override
    public UserResponse disable(Long id) {
        User user = getUser(id);
        user.setStatus(UserStatus.DISABLED);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return toResponse(user);
    }

    @Override
    public UserResponse enable(Long id) {
        User user = getUser(id);
        user.setStatus(UserStatus.ENABLED);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return toResponse(user);
    }

    private User getUser(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User id must not be null");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User does not exist");
        }
        return user;
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("limit 1"));
    }

    private boolean existsByUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUid(),
                user.getUsername(),
                user.getNickname(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private UserRole defaultRole(UserRole role) {
        return role == null ? UserRole.STUDENT : role;
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
