package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.domain.dto.CreateUserRequest;
import com.example.domain.dto.LoginRequest;
import com.example.domain.po.User;
import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import com.example.exception.BusinessException;
import com.example.mapper.UserMapper;
import com.example.service.imp.UserServiceImpl;
import com.example.tool.PasswordTool;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class UserServiceImplTest {

    private UserService userService;
    private Map<Long, User> users;
    private AtomicLong idGenerator;

    @BeforeEach
    void setUp() {
        UserMapper userMapper = mock(UserMapper.class);
        users = new HashMap<>();
        idGenerator = new AtomicLong();
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUid(idGenerator.incrementAndGet());
            users.put(user.getUid(), user);
            return 1;
        });
        when(userMapper.updateById(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            users.put(user.getUid(), user);
            return 1;
        });
        when(userMapper.selectById(any(Long.class))).thenAnswer(invocation -> users.get(invocation.getArgument(0)));
        when(userMapper.selectOne(any())).thenAnswer(invocation -> findAlice());
        when(userMapper.selectCount(any())).thenAnswer(invocation -> findAlice() == null ? 0L : 1L);
        userService = new UserServiceImpl(userMapper, new PasswordTool());
    }

    @Test
    void passwordHash() {
        PasswordTool passwordTool = new PasswordTool();
        assertThat(passwordTool.hash("12345678")).isNotBlank();
    }

    @Test
    void createAndLoginUserDefaultsToStudent() {
        var created = userService.create(new CreateUserRequest(
                "alice",
                "123456",
                "Alice",
                "13800000000",
                "alice@example.com",
                null
        ));

        var loggedIn = userService.login(new LoginRequest("alice", "123456"));

        assertThat(created.id()).isEqualTo(loggedIn.id());
        assertThat(created.role()).isEqualTo(UserRole.STUDENT);
        assertThat(loggedIn.role()).isEqualTo(UserRole.STUDENT);
        assertThat(loggedIn.status()).isEqualTo(UserStatus.ENABLED);
    }

    @Test
    void createTeacherUser() {
        var created = userService.create(new CreateUserRequest(
                "alice",
                "123456",
                "Alice",
                "13800000000",
                "alice@example.com",
                UserRole.TEACHER
        ));

        assertThat(created.role()).isEqualTo(UserRole.TEACHER);
    }

    @Test
    void rejectDuplicateUsername() {
        userService.create(new CreateUserRequest("alice", "123456", null, null, null, null));

        assertThatThrownBy(() -> userService.create(new CreateUserRequest("alice", "abcdef", null, null, null, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void disabledUserCannotLogin() {
        var created = userService.create(new CreateUserRequest("alice", "123456", null, null, null, null));
        userService.disable(created.id());

        assertThatThrownBy(() -> userService.login(new LoginRequest("alice", "123456")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    private User findAlice() {
        return users.values().stream()
                .filter(user -> user.getUsername().equals("alice"))
                .findFirst()
                .orElse(null);
    }
}
