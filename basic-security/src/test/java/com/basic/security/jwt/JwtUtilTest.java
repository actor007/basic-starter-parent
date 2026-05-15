package com.basic.security.jwt;

import com.basic.security.core.model.LoginUser;
import com.basic.security.core.properties.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtUtil 单元测试
 *
 * @author actor
 */
@DisplayName("JwtUtil JWT工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private LoginUser testUser;
    private SecurityProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SecurityProperties();
        properties.getJwt().setSecret("ThisIsAVeryLongSecretKeyForTestingAtLeast32Bytes!");
        properties.getJwt().setExpiration(60000L); // 1 minute
        properties.getJwt().setRefreshExpiration(120000L); // 2 minutes

        jwtUtil = new JwtUtil(properties);

        testUser = LoginUser.builder()
                .userId("user-001")
                .username("actor")
                .tenantId("tenant-01")
                .userType("admin")
                .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                .permissions(Set.of("user:read", "user:write"))
                .build();
    }

    // ========== generateAccessToken ==========

    @Nested
    @DisplayName("generateAccessToken - 生成 Access Token")
    class GenerateAccessToken {

        @Test
        @DisplayName("应生成非空 Token")
        void shouldGenerateNonNullToken() {
            String token = jwtUtil.generateAccessToken(testUser);
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("生成的 Token 应有三段")
        void shouldHaveThreeParts() {
            String token = jwtUtil.generateAccessToken(testUser);
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Token 应包含 userId 作为 subject")
        void shouldContainUserIdAsSubject() {
            String token = jwtUtil.generateAccessToken(testUser);
            String userId = jwtUtil.getUserIdFromToken(token);
            assertThat(userId).isEqualTo("user-001");
        }

        @Test
        @DisplayName("Token 应包含 username claim")
        void shouldContainUsernameClaim() {
            String token = jwtUtil.generateAccessToken(testUser);
            Claims claims = jwtUtil.parseToken(token);
            assertThat(claims.get("username", String.class)).isEqualTo("actor");
        }

        @Test
        @DisplayName("Token 应包含 tenantId claim")
        void shouldContainTenantIdClaim() {
            String token = jwtUtil.generateAccessToken(testUser);
            Claims claims = jwtUtil.parseToken(token);
            assertThat(claims.get("tenantId", String.class)).isEqualTo("tenant-01");
        }

        @Test
        @DisplayName("Token 应包含 userType claim")
        void shouldContainUserTypeClaim() {
            String token = jwtUtil.generateAccessToken(testUser);
            Claims claims = jwtUtil.parseToken(token);
            assertThat(claims.get("userType", String.class)).isEqualTo("admin");
        }

        @Test
        @DisplayName("Token 应包含 roles claim（逗号分隔）")
        void shouldContainRolesClaimAsCommaSeparated() {
            String token = jwtUtil.generateAccessToken(testUser);
            Claims claims = jwtUtil.parseToken(token);
            String roles = claims.get("roles", String.class);
            assertThat(roles).contains("ROLE_ADMIN").contains("ROLE_USER");
        }

        @Test
        @DisplayName("Token 应包含 iat 和 exp")
        void shouldContainIatAndExp() {
            String token = jwtUtil.generateAccessToken(testUser);
            Claims claims = jwtUtil.parseToken(token);
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiration()).isNotNull();
        }
    }

    // ========== generateRefreshToken ==========

    @Nested
    @DisplayName("generateRefreshToken - 生成 Refresh Token")
    class GenerateRefreshToken {

        @Test
        @DisplayName("应生成非空 Refresh Token")
        void shouldGenerateNonNullRefreshToken() {
            String token = jwtUtil.generateRefreshToken(testUser);
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("Refresh Token 应包含 type=refresh")
        void shouldContainRefreshTypeClaim() {
            String token = jwtUtil.generateRefreshToken(testUser);
            Claims claims = jwtUtil.parseToken(token);
            assertThat(claims.get("type", String.class)).isEqualTo("refresh");
        }

        @Test
        @DisplayName("Refresh Token subject 应为 userId")
        void shouldHaveUserIdAsSubject() {
            String token = jwtUtil.generateRefreshToken(testUser);
            assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo("user-001");
        }

        @Test
        @DisplayName("Access Token 和 Refresh Token 应不同")
        void accessAndRefreshShouldBeDifferent() {
            String access = jwtUtil.generateAccessToken(testUser);
            String refresh = jwtUtil.generateRefreshToken(testUser);
            assertThat(access).isNotEqualTo(refresh);
        }
    }

    // ========== parseToken ==========

    @Nested
    @DisplayName("parseToken - 解析 Token")
    class ParseToken {

        @Test
        @DisplayName("应正确解析有效 Token")
        void shouldParseValidToken() {
            String token = jwtUtil.generateAccessToken(testUser);
            Claims claims = jwtUtil.parseToken(token);
            assertThat(claims.getSubject()).isEqualTo("user-001");
        }

        @Test
        @DisplayName("应能解析 Token 后重建 LoginUser")
        void shouldRebuildLoginUserFromToken() {
            String token = jwtUtil.generateAccessToken(testUser);
            LoginUser result = jwtUtil.getLoginUserFromToken(token);
            assertThat(result.getUserId()).isEqualTo("user-001");
            assertThat(result.getUsername()).isEqualTo("actor");
            assertThat(result.getTenantId()).isEqualTo("tenant-01");
            assertThat(result.getRoles()).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
        }

        @Test
        @DisplayName("解析无效 Token 应抛出 JwtException")
        void shouldThrowOnInvalidToken() {
            assertThatThrownBy(() -> jwtUtil.parseToken("invalid.token.here"))
                    .isInstanceOf(io.jsonwebtoken.JwtException.class);
        }

        @Test
        @DisplayName("解析空 Token 应抛出异常")
        void shouldThrowOnEmptyToken() {
            assertThatThrownBy(() -> jwtUtil.parseToken(""))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("解析 null Token 应抛出异常")
        void shouldThrowOnNullToken() {
            assertThatThrownBy(() -> jwtUtil.parseToken(null))
                    .isInstanceOf(Exception.class);
        }
    }

    // ========== validateToken ==========

    @Nested
    @DisplayName("validateToken - 校验 Token")
    class ValidateToken {

        @Test
        @DisplayName("有效 Token 应校验通过")
        void shouldValidateValidToken() {
            String token = jwtUtil.generateAccessToken(testUser);
            assertThat(jwtUtil.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("无效 Token 应校验失败")
        void shouldFailOnInvalidToken() {
            assertThat(jwtUtil.validateToken("invalid")).isFalse();
        }

        @Test
        @DisplayName("被篡改的 Token 应校验失败")
        void shouldFailOnTamperedToken() {
            String token = jwtUtil.generateAccessToken(testUser);
            String tampered = token.substring(0, token.length() - 2) + "xx";
            assertThat(jwtUtil.validateToken(tampered)).isFalse();
        }
    }

    // ========== getUserIdFromToken ==========

    @Nested
    @DisplayName("getUserIdFromToken - 从 Token 获取用户ID")
    class GetUserIdFromToken {

        @Test
        @DisplayName("应正确提取 userId")
        void shouldExtractUserId() {
            String token = jwtUtil.generateAccessToken(testUser);
            assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo("user-001");
        }

        @Test
        @DisplayName("无效 Token 应抛出异常")
        void shouldThrowOnInvalid() {
            assertThatThrownBy(() -> jwtUtil.getUserIdFromToken("bad"))
                    .isInstanceOf(io.jsonwebtoken.JwtException.class);
        }
    }

    // ========== getLoginUserFromToken ==========

    @Nested
    @DisplayName("getLoginUserFromToken - 从 Token 构建 LoginUser")
    class GetLoginUserFromToken {

        @Test
        @DisplayName("应完整重建 LoginUser 对象")
        void shouldFullyRebuildLoginUser() {
            String token = jwtUtil.generateAccessToken(testUser);
            LoginUser result = jwtUtil.getLoginUserFromToken(token);

            assertThat(result.getUserId()).isEqualTo("user-001");
            assertThat(result.getUsername()).isEqualTo("actor");
            assertThat(result.getTenantId()).isEqualTo("tenant-01");
            assertThat(result.getUserType()).isEqualTo("admin");
            assertThat(result.getRoles()).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
        }
    }

    // ========== 边界情况 ==========

    @Nested
    @DisplayName("边界情况")
    class EdgeCases {

        @Test
        @DisplayName("用户 roles 为空时不应报错")
        void shouldHandleEmptyRoles() {
            LoginUser user = LoginUser.builder()
                    .userId("user-empty")
                    .username("empty")
                    .roles(null)
                    .build();
            String token = jwtUtil.generateAccessToken(user);
            Claims claims = jwtUtil.parseToken(token);
            assertThat(claims.get("roles", String.class)).isEmpty();
        }

        @Test
        @DisplayName("用户 username 为 null 时不应报错")
        void shouldHandleNullUsername() {
            LoginUser user = LoginUser.builder()
                    .userId("user-nullname")
                    .username(null)
                    .build();
            String token = jwtUtil.generateAccessToken(user);
            Claims claims = jwtUtil.parseToken(token);
            assertThat(claims.get("username", String.class)).isEmpty();
        }

        @Test
        @DisplayName("相同用户两次生成的 Token 应有不同的 jti")
        void shouldHaveDifferentJtiForSameUser() {
            String token1 = jwtUtil.generateAccessToken(testUser);
            String token2 = jwtUtil.generateAccessToken(testUser);
            Claims c1 = jwtUtil.parseToken(token1);
            Claims c2 = jwtUtil.parseToken(token2);
            assertThat(c1.getId()).isNotEqualTo(c2.getId());
        }

        @Test
        @DisplayName("过期 Token 的 ExpiredJwtException 不应被 validateToken 抛出")
        void validateTokenShouldReturnFalseForExpired() {
            // 使用极短过期时间
            properties.getJwt().setExpiration(1L); // 1ms
            JwtUtil shortLivedJwt = new JwtUtil(properties);
            String token = shortLivedJwt.generateAccessToken(testUser);
            // 1ms 后已过期
            assertThat(shortLivedJwt.validateToken(token)).isFalse();
        }
    }
}
