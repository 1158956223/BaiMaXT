package com.example.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final ObjectMapper objectMapper;
    private final String secret;

    public JwtValidator(ObjectMapper objectMapper, @Value("${auth.jwt.secret}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret;
    }

    public JwtPayload validate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token format");
            }
            String unsigned = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(unsigned).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Invalid token signature");
            }

            Map<String, Object> payload = objectMapper.readValue(
                    URL_DECODER.decode(parts[1]),
                    new TypeReference<>() {
                    }
            );
            long exp = asLong(payload.get("exp"));
            if (Instant.now().getEpochSecond() >= exp) {
                throw new IllegalArgumentException("Token expired");
            }

            return new JwtPayload(
                    asLong(payload.get("accountId")),
                    asLong(payload.get("userId")),
                    asString(payload.get("username")),
                    asString(payload.get("role"))
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token", exception);
        }
    }

    private String sign(String unsigned) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        return URL_ENCODER.encodeToString(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
    }

    private long asLong(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing number claim");
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String asString(Object value) {
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Missing string claim");
        }
        return value.toString();
    }
}
