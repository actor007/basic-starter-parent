package com.basic.security.jwt;

import com.basic.security.core.constant.SecurityConstant;
import com.basic.security.core.exception.SecurityException;
import com.basic.security.core.model.LoginUser;
import com.basic.security.core.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Token 服务
 * <p>
 * 管理 Token 的生成、存储（Redis）、刷新与吊销。
 *
 * @author actor
 */
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final SecurityProperties securityProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建 Token 并存入 Redis
     */
    public TokenPair createTokens(LoginUser loginUser) {
        String accessToken = jwtUtil.generateAccessToken(loginUser);
        String refreshToken = jwtUtil.generateRefreshToken(loginUser);

        // 存入 Redis
        long expire = securityProperties.getJwt().getExpiration();
        long refreshExpire = securityProperties.getJwt().getRefreshExpiration();

        String accessKey = SecurityConstant.REDIS_TOKEN_KEY + loginUser.getUserId();
        String refreshKey = SecurityConstant.REDIS_REFRESH_TOKEN_KEY + loginUser.getUserId();

        redisTemplate.opsForValue().set(accessKey, accessToken, expire, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(refreshKey, refreshToken, refreshExpire, TimeUnit.MILLISECONDS);

        log.debug("Token created for user: {}", loginUser.getUserId());
        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * 验证 Access Token
     */
    public LoginUser validateAccessToken(String token) {
        LoginUser loginUser = jwtUtil.getLoginUserFromToken(token);

        // 检查 Redis 中是否存在（未吊销）
        String key = SecurityConstant.REDIS_TOKEN_KEY + loginUser.getUserId();
        Object stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            throw new SecurityException.UnauthorizedException("Token已失效");
        }

        return loginUser;
    }

    /**
     * 刷新 Token
     */
    public TokenPair refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new SecurityException.TokenExpiredException("Refresh Token无效或已过期");
        }
        LoginUser loginUser = jwtUtil.getLoginUserFromToken(refreshToken);

        // 检查 Redis
        String refreshKey = SecurityConstant.REDIS_REFRESH_TOKEN_KEY + loginUser.getUserId();
        Object stored = redisTemplate.opsForValue().get(refreshKey);
        if (stored == null) {
            throw new SecurityException.UnauthorizedException("Refresh Token已失效");
        }

        return createTokens(loginUser);
    }

    /**
     * 吊销 Token（登出）
     */
    public void revokeToken(String userId) {
        redisTemplate.delete(SecurityConstant.REDIS_TOKEN_KEY + userId);
        redisTemplate.delete(SecurityConstant.REDIS_REFRESH_TOKEN_KEY + userId);
        log.info("Token revoked for user: {}", userId);
    }

    /**
     * Token 对
     */
    public record TokenPair(String accessToken, String refreshToken) {
    }

}
