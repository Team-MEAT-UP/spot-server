package com.meetup.server.auth.infrastructure;

import com.meetup.server.auth.exception.AuthErrorType;
import com.meetup.server.auth.exception.AuthException;
import com.meetup.server.global.support.jwt.JwtProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Collections;
import java.util.HexFormat;

@Repository
public class RefreshTokenRedisRepository {

    private static final String KEY_PREFIX = "auth:refresh-token:";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final RedisTemplate<String, String> redisRefreshTokenTemplate;
    private final RedisScript<Long> refreshTokenRotateScript;
    private final JwtProperties jwtProperties;
    private final String redisKeyPrefix;

    public RefreshTokenRedisRepository(
            @Qualifier("redisRefreshTokenTemplate") RedisTemplate<String, String> redisRefreshTokenTemplate,
            @Qualifier("refreshTokenRotateScript") RedisScript<Long> refreshTokenRotateScript,
            JwtProperties jwtProperties,
            @Value("${spring.data.redis.key-prefix:}") String redisKeyPrefix
    ) {
        this.redisRefreshTokenTemplate = redisRefreshTokenTemplate;
        this.refreshTokenRotateScript = refreshTokenRotateScript;
        this.jwtProperties = jwtProperties;
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public void save(Long userId, String refreshToken) {
        redisRefreshTokenTemplate.opsForValue()
                .set(createKey(userId), digest(refreshToken), getRefreshTokenTtl());
    }

    public boolean rotate(Long userId, String oldRefreshToken, String newRefreshToken) {
        Long result = redisRefreshTokenTemplate.execute(
                refreshTokenRotateScript,
                Collections.singletonList(createKey(userId)),
                digest(oldRefreshToken),
                digest(newRefreshToken),
                String.valueOf(getRefreshTokenTtl().toSeconds())
        );

        return Long.valueOf(1L).equals(result);
    }

    public void delete(Long userId) {
        redisRefreshTokenTemplate.delete(createKey(userId));
    }

    public String digest(String refreshToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);
            return HexFormat.of().formatHex(mac.doFinal(refreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new AuthException(AuthErrorType.FAILED_REFRESH_TOKEN_DIGEST);
        }
    }

    private String createKey(Long userId) {
        return (redisKeyPrefix != null ? redisKeyPrefix : "") + KEY_PREFIX + userId;
    }

    private Duration getRefreshTokenTtl() {
        return Duration.ofMillis(jwtProperties.refreshTokenExpiration());
    }
}
