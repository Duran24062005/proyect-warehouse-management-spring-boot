package com.proyectS1.warehouse_management.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectS1.warehouse_management.model.AppUser;

@Service
public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final byte[] secretKey;
    private final long expirationMillis;

    public JwtTokenService(
        ObjectMapper objectMapper,
        @Value("${app.security.jwt.secret}") String secret,
        @Value("${app.security.jwt.expiration-millis}") long expirationMillis
    ) {
        this.objectMapper = objectMapper;
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(AppUser user) {
        long now = Instant.now().toEpochMilli();
        long expiration = now + expirationMillis;

        Map<String, Object> header = Map.of(
            "alg", "HS256",
            "typ", "JWT"
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getEmail());
        payload.put("role", user.getRole().name());
        payload.put("iat", now);
        payload.put("exp", expiration);

        String encodedHeader = base64UrlEncode(writeJson(header));
        String encodedPayload = base64UrlEncode(writeJson(payload));
        String signature = sign(encodedHeader + "." + encodedPayload);

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public String extractUsername(String token) {
        return readClaims(token).get("sub").toString();
    }

    public boolean isTokenValid(String token) {
        Map<String, Object> claims = readClaims(token);
        long expiration = Long.parseLong(claims.get("exp").toString());
        return expiration > Instant.now().toEpochMilli();
    }

    private Map<String, Object> readClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String signedContent = parts[0] + "." + parts[1];
        String expectedSignature = sign(signedContent);
        if (!expectedSignature.equals(parts[2])) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(payloadBytes, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid JWT payload", exception);
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey, HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign JWT", exception);
        }
    }

    private byte[] writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to serialize JWT content", exception);
        }
    }

    private String base64UrlEncode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
