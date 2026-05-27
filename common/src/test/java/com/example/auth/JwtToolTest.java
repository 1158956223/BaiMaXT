package com.example.auth;

import com.example.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtToolTest {

    private static final String SECRET = "test-secret";
    private final JwtTool jwtTool = new JwtTool(new ObjectMapper(), SECRET, 600);

    @Test
    void createsAndValidatesTokenPayload() {
        String token = jwtTool.createToken(1L, 42L, "student", UserRole.STUDENT, "jti-1");

        JwtPayload payload = jwtTool.validate(token);

        assertThat(payload.accountId()).isEqualTo(1L);
        assertThat(payload.userId()).isEqualTo(42L);
        assertThat(payload.username()).isEqualTo("student");
        assertThat(payload.role()).isEqualTo("STUDENT");
        assertThat(payload.jti()).isEqualTo("jti-1");
        assertThat(payload.exp()).isGreaterThan(0L);
    }

    @Test
    void parseTokenKeepsExistingMapBasedAuthServiceContract() {
        String token = jwtTool.createToken(1L, 42L, "student", UserRole.STUDENT, "jti-1");

        Map<String, Object> payload = jwtTool.parseToken(token);

        assertThat(jwtTool.getLong(payload, "userId")).isEqualTo(42L);
        assertThat(jwtTool.getString(payload, "role")).isEqualTo("STUDENT");
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtTool.createToken(1L, 42L, "student", UserRole.STUDENT, "jti-1");

        assertThatThrownBy(() -> jwtTool.validate(token + "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid token");
    }
}
