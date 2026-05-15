package com.basic.security.jwt;

import com.basic.security.core.constant.SecurityConstant;
import com.basic.security.core.exception.SecurityException;
import com.basic.security.core.model.LoginUser;
import com.basic.security.core.properties.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TokenService 单元测试
 *
 * @author actor
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TokenService Token服务测试")
class TokenServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private SecurityProperties securityProperties;
    private TokenService tokenService;
    private LoginUser testUser;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        securityProperties.getJwt().setExpiration(86400000L);
        securityProperties.getJwt().setRefreshExpiration(604800000L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenService = new TokenService(jwtUtil, securityProperties, redisTemplate);

        testUser = LoginUser.builder()
                .userId("user-001")
                .username("actor")
                .tenantId("tenant-01")
                .userType("admin")
                .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                .build();
    }

    // ========== createTokens ==========

    @Nested
    @DisplayName("createTokens - 创建 Token 对")
    class CreateTokens {

        @Test
        @DisplayName("应创建非空的 access 和 refresh token")
        void shouldCreateBothTokens() {
            when(jwtUtil.generateAccessToken(testUser)).thenReturn("access-token-xxx");
            when(jwtUtil.generateRefreshToken(testUser)).thenReturn("refresh-token-xxx");

            TokenService.TokenPair pair = tokenService.createTokens(testUser);

            assertThat(pair.accessToken()).isEqualTo("access-token-xxx");
            assertThat(pair.refreshToken()).isEqualTo("refresh-token-xxx");
        }

        @Test
        @DisplayName("应将 access token 存入 Redis")
        void shouldStoreAccessTokenInRedis() {
            when(jwtUtil.generateAccessToken(testUser)).thenReturn("access-token-xxx");
            when(jwtUtil.generateRefreshToken(testUser)).thenReturn("refresh-token-xxx");

            tokenService.createTokens(testUser);

            String expectedKey = SecurityConstant.REDIS_TOKEN_KEY + "user-001";
            verify(valueOperations).set(eq(expectedKey), eq("access-token-xxx"), anyLong(), eq(TimeUnit.MILLISECONDS));
        }

        @Test
        @DisplayName("应将 refresh token 存入 Redis")
        void shouldStoreRefreshTokenInRedis() {
            when(jwtUtil.generateAccessToken(testUser)).thenReturn("access-token-xxx");
            when(jwtUtil.generateRefreshToken(testUser)).thenReturn("refresh-token-xxx");

            tokenService.createTokens(testUser);

            String expectedKey = SecurityConstant.REDIS_REFRESH_TOKEN_KEY + "user-001";
            verify(valueOperations).set(eq(expectedKey), eq("refresh-token-xxx"), anyLong(), eq(TimeUnit.MILLISECONDS));
        }
    }

    // ========== validateAccessToken ==========

    @Nested
    @DisplayName("validateAccessToken - 校验 Access Token")
    class ValidateAccessToken {

        @Test
        @DisplayName("有效 Token 应返回 LoginUser")
        void shouldReturnLoginUserWhenValid() {
            when(jwtUtil.getLoginUserFromToken("valid-token")).thenReturn(testUser);
            String key = SecurityConstant.REDIS_TOKEN_KEY + "user-001";
            when(valueOperations.get(key)).thenReturn("valid-token");

            LoginUser result = tokenService.validateAccessToken("valid-token");
            assertThat(result.getUserId()).isEqualTo("user-001");
        }

        @Test
        @DisplayName("Redis 中不存在 Token 时应抛出 UnauthorizedException")
        void shouldThrowWhenTokenNotInRedis() {
            when(jwtUtil.getLoginUserFromToken("stale-token")).thenReturn(testUser);
            String key = SecurityConstant.REDIS_TOKEN_KEY + "user-001";
            when(valueOperations.get(key)).thenReturn(null);

            assertThatThrownBy(() -> tokenService.validateAccessToken("stale-token"))
                    .isInstanceOf(SecurityException.UnauthorizedException.class)
                    .hasMessageContaining("Token已失效");
        }
    }

    // ========== refreshToken ==========

    @Nested
    @DisplayName("refreshToken - 刷新 Token")
    class RefreshToken {

        @Test
        @DisplayName("有效 Refresh Token 应返回新的 Token 对")
        void shouldReturnNewTokensWhenRefreshValid() {
            when(jwtUtil.validateToken("refresh-token")).thenReturn(true);
            when(jwtUtil.getLoginUserFromToken("refresh-token")).thenReturn(testUser);

            String refreshKey = SecurityConstant.REDIS_REFRESH_TOKEN_KEY + "user-001";
            when(valueOperations.get(refreshKey)).thenReturn("refresh-token");

            when(jwtUtil.generateAccessToken(testUser)).thenReturn("new-access");
            when(jwtUtil.generateRefreshToken(testUser)).thenReturn("new-refresh");

            TokenService.TokenPair result = tokenService.refreshToken("refresh-token");
            assertThat(result.accessToken()).isEqualTo("new-access");
            assertThat(result.refreshToken()).isEqualTo("new-refresh");
        }

        @Test
        @DisplayName("Refresh Token 无效时应抛出 TokenExpiredException")
        void shouldThrowWhenRefreshTokenInvalid() {
            when(jwtUtil.validateToken("bad-refresh")).thenReturn(false);

            assertThatThrownBy(() -> tokenService.refreshToken("bad-refresh"))
                    .isInstanceOf(SecurityException.TokenExpiredException.class)
                    .hasMessageContaining("Refresh Token无效或已过期");
        }

        @Test
        @DisplayName("Refresh Token 不在 Redis 中时应抛出 UnauthorizedException")
        void shouldThrowWhenRefreshNotInRedis() {
            when(jwtUtil.validateToken("refresh-token")).thenReturn(true);
            when(jwtUtil.getLoginUserFromToken("refresh-token")).thenReturn(testUser);

            String refreshKey = SecurityConstant.REDIS_REFRESH_TOKEN_KEY + "user-001";
            when(valueOperations.get(refreshKey)).thenReturn(null);

            assertThatThrownBy(() -> tokenService.refreshToken("refresh-token"))
                    .isInstanceOf(SecurityException.UnauthorizedException.class)
                    .hasMessageContaining("Refresh Token已失效");
        }
    }

    // ========== revokeToken ==========

    @Nested
    @DisplayName("revokeToken - 吊销 Token")
    class RevokeToken {

        @Test
        @DisplayName("应删除 Redis 中的 access 和 refresh token")
        void shouldDeleteBothTokensFromRedis() {
            tokenService.revokeToken("user-001");

            String accessKey = SecurityConstant.REDIS_TOKEN_KEY + "user-001";
            String refreshKey = SecurityConstant.REDIS_REFRESH_TOKEN_KEY + "user-001";

            verify(redisTemplate).delete(accessKey);
            verify(redisTemplate).delete(refreshKey);
        }
    }
}
