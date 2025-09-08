package com.ecommerce.backend.modules.auth.jwt;

import com.ecommerce.backend.modules.auth.token.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {
    private final JwtProps jwtProps;
    private final RedisTemplate<String, String> redisTemplate;
    private final RefreshTokenService refreshTokenService;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProps.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserDetails userDetails, Long userId) {
        log.info("Generating access token for user with email: {}", userDetails.getUsername());
        return generateToken(userDetails, userId, jwtProps.getAccessTokenExpiration(), "access");
    }

    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        log.info("Generating refresh token for user with email: {}", userDetails.getUsername());
        String refreshToken = generateToken(userDetails, userId, jwtProps.getRefreshTokenExpiration(), "refresh");

        refreshTokenService.saveRefreshToken(refreshToken, userId);

        return refreshToken;
    }

    private String generateToken(UserDetails userDetails, Long userId, long expiration, String type) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuer(jwtProps.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .claim("userId", userId)
                .claim("authorities", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(joining(",")))
                .claim("tokenType", type)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String authorities = claims.get("authorities", String.class);

        if (authorities == null || authorities.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(authorities.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims.getExpiration().before(new Date())) {
                log.warn("Token has expired");
                return false;
            }

            if (isTokenBlackListed(token)) {
                log.warn("Token is blacklisted");
                return false;
            }

            String tokenType = claims.get("tokenType", String.class);
            if ("refresh".equals(tokenType)) {
                return refreshTokenService.isTokenValid(token);
            }

            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();

            long timeToLive = expiration.getTime() - System.currentTimeMillis();

            if (timeToLive > 0) {
                String blacklistKey = "blacklisted_token:" + token;
                redisTemplate.opsForValue().set(
                        blacklistKey,
                        "true",
                        Duration.ofMillis(timeToLive)
                );
            }
        } catch (Exception e) {
            log.error("Token blacklist failed: {}", e.getMessage());
        }
    }

    public boolean isTokenBlackListed(String token) {
        String blacklistKey = "blacklisted_token:" + token;
        return redisTemplate.hasKey(blacklistKey);
    }

    public void revokeRefreshToken(Long userId) {
        refreshTokenService.revokeAllUserTokens(userId);
    }

    public String rotateRefreshToken(String oldRefreshToken, UserDetails userDetails, Long userId) {
        log.info("Rotating refresh token for user with email: {}", userDetails.getUsername());
        String newRefreshToken = generateToken(userDetails, userId, jwtProps.getRefreshTokenExpiration(), "refresh");

        refreshTokenService.rotateToken(oldRefreshToken, newRefreshToken);

        log.info("Refresh token rotated successfully for user: {}", userDetails.getUsername());
        return newRefreshToken;
    }
}
