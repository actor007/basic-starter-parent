package com.basic.security.jwt;

import com.basic.security.core.model.LoginUser;
import com.basic.security.core.properties.SecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.*;

/**
 * JWT 工具类
 * <p>
 * 使用 jjwt 0.12.x API，支持 access token + refresh token。
 *
 * @author actor
 */
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    private final SecurityProperties securityProperties;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                Base64.getEncoder().encodeToString(securityProperties.getJwt().getSecret().getBytes())));
    }

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(LoginUser loginUser) {
        SecurityProperties.JwtProperties jwt = securityProperties.getJwt();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(loginUser.getUserId())
                .claims(Map.of(
                        "username", loginUser.getUsername() != null ? loginUser.getUsername() : "",
                        "tenantId", loginUser.getTenantId() != null ? loginUser.getTenantId() : "",
                        "userType", loginUser.getUserType() != null ? loginUser.getUserType() : "",
                        "roles", loginUser.getRoles() != null ? String.join(",", loginUser.getRoles()) : ""
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwt.getExpiration()))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(LoginUser loginUser) {
        SecurityProperties.JwtProperties jwt = securityProperties.getJwt();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(loginUser.getUserId())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwt.getRefreshExpiration()))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 从 Token 解析 Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("JWT parse error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 从 Token 获取用户ID
     */
    public String getUserIdFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 构建 LoginUser
     */
    public LoginUser getLoginUserFromToken(String token) {
        Claims claims = parseToken(token);
        return LoginUser.builder()
                .userId(claims.getSubject())
                .username(claims.get("username", String.class))
                .tenantId(claims.get("tenantId", String.class))
                .userType(claims.get("userType", String.class))
                .roles(parseSet(claims.get("roles", String.class)))
                .build();
    }

    private Set<String> parseSet(String str) {
        if (str == null || str.isEmpty()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(str.split(",")));
    }

}
