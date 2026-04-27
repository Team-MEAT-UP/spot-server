package com.meetup.server.global.support.jwt;

import com.meetup.server.auth.dto.CustomOAuth2User;
import com.meetup.server.auth.dto.response.JwtUserDetails;
import com.meetup.server.auth.exception.AuthErrorType;
import com.meetup.server.auth.exception.AuthException;
import com.meetup.server.user.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private Key key;

    @PostConstruct
    public void setKey() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes());
    }

    public String createAccessToken(Object user) {
        return createToken(user, jwtProperties.accessTokenExpiration(), null);
    }

    public String createRefreshToken(Object user) {
        return createToken(user, jwtProperties.refreshTokenExpiration(), UUID.randomUUID().toString());
    }

    private String createToken(Object user, long expiration, String tokenId) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + expiration);

        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .setIssuer(jwtProperties.issuer())
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .signWith(key, SignatureAlgorithm.HS512);

        if (tokenId != null) {
            builder.setId(tokenId);
        }

        if (user instanceof CustomOAuth2User oAuth2User) {
            builder.setSubject(oAuth2User.getUserId().toString());
        } else if (user instanceof User normalUser) {
            builder.setSubject(normalUser.getUserId().toString());
        } else {
            throw new AuthException(AuthErrorType.FAILED_TOKEN_CREATION);
        }

        return builder.compact();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            log.info("login user: {}", claims.getBody().getSubject());
            return claims.getBody().getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token validation error: ", e);
            return false;
        }
    }

    public JwtUserDetails getJwtUserDetails(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return JwtUserDetails.fromClaim(claims);
    }

    public Long extractUserIdFromToken(String token) {
        try {
            return Long.parseLong(Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject());
        } catch (JwtException | NumberFormatException e) {
            throw new AuthException(AuthErrorType.INVALID_TOKEN);
        }
    }
}
