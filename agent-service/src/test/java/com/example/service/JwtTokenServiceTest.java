package com.example.service;

import com.example.auth.JwtTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "test-secret";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtTokenService jwtTokenService = new JwtTokenService(new JwtTool(objectMapper, SECRET, 600));

    @Test
    void parsesUserIdFromBearerToken() throws Exception {
        String token = createToken(Map.of(
                "accountId", 1L,
                "userId", 42L,
                "username", "student",
                "role", "STUDENT",
                "jti", "jti-1",
                "exp", Instant.now().plusSeconds(600).getEpochSecond()
        ));

        Long userId = jwtTokenService.parseUserId("Bearer " + token);

        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void rejectsMissingBearerToken() {
        assertThatThrownBy(() -> jwtTokenService.parseUserId(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or missing token");
    }

    private String createToken(Map<String, Object> payloadValues) throws Exception {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        String unsigned = encode(header) + "." + encode(payloadValues);
        return unsigned + "." + sign(unsigned);
    }

    private String encode(Map<String, Object> value) throws Exception {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String unsigned) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
    }
}
