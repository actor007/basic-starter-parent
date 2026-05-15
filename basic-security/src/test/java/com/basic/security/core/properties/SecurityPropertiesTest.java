package com.basic.security.core.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityProperties 单元测试
 *
 * @author actor
 */
@DisplayName("SecurityProperties 配置属性测试")
class SecurityPropertiesTest {

    private final SecurityProperties props = new SecurityProperties();

    // ========== 默认值 ==========

    @Nested
    @DisplayName("默认值")
    class DefaultValues {

        @Test
        @DisplayName("authMode 默认应为 jwt")
        void authModeShouldDefaultToJwt() {
            assertThat(props.getAuthMode()).isEqualTo("jwt");
        }

        @Test
        @DisplayName("anonymousUrls 默认应为空列表")
        void anonymousUrlsShouldBeEmpty() {
            assertThat(props.getAnonymousUrls()).isEmpty();
        }

        @Test
        @DisplayName("tenantEnabled 默认应为 false")
        void tenantEnabledShouldBeFalse() {
            assertThat(props.isTenantEnabled()).isFalse();
        }
    }

    // ========== JWT 配置默认值 ==========

    @Nested
    @DisplayName("JWT 配置默认值")
    class JwtDefaults {

        @Test
        @DisplayName("expiration 默认应为 86400000ms（24h）")
        void expirationShouldBe24h() {
            assertThat(props.getJwt().getExpiration()).isEqualTo(86400000L);
        }

        @Test
        @DisplayName("refreshExpiration 默认应为 604800000ms（7d）")
        void refreshExpirationShouldBe7d() {
            assertThat(props.getJwt().getRefreshExpiration()).isEqualTo(604800000L);
        }

        @Test
        @DisplayName("tokenHeader 默认应为 Authorization")
        void tokenHeaderShouldBeAuthorization() {
            assertThat(props.getJwt().getTokenHeader()).isEqualTo("Authorization");
        }

        @Test
        @DisplayName("tokenPrefix 默认应为 Bearer ")
        void tokenPrefixShouldBeBearer() {
            assertThat(props.getJwt().getTokenPrefix()).isEqualTo("Bearer ");
        }

        @Test
        @DisplayName("secret 应有非空默认值")
        void secretShouldBeNonNull() {
            assertThat(props.getJwt().getSecret()).isNotBlank();
        }
    }

    // ========== 验证码配置默认值 ==========

    @Nested
    @DisplayName("验证码配置默认值")
    class CaptchaDefaults {

        @Test
        @DisplayName("enabled 默认应为 false")
        void enabledShouldBeFalse() {
            assertThat(props.getCaptcha().isEnabled()).isFalse();
        }

        @Test
        @DisplayName("type 默认应为 arithmetic")
        void typeShouldBeArithmetic() {
            assertThat(props.getCaptcha().getType()).isEqualTo("arithmetic");
        }

        @Test
        @DisplayName("expireSeconds 默认应为 120")
        void expireSecondsShouldBe120() {
            assertThat(props.getCaptcha().getExpireSeconds()).isEqualTo(120L);
        }

        @Test
        @DisplayName("store 默认应为 memory")
        void storeShouldBeMemory() {
            assertThat(props.getCaptcha().getStore()).isEqualTo("memory");
        }
    }

    // ========== API 签名配置默认值 ==========

    @Nested
    @DisplayName("API 签名配置默认值")
    class ApiSignDefaults {

        @Test
        @DisplayName("enabled 默认应为 false")
        void enabledShouldBeFalse() {
            assertThat(props.getApiSign().isEnabled()).isFalse();
        }

        @Test
        @DisplayName("algorithm 默认应为 HMAC-SHA256")
        void algorithmShouldBeHmacSha256() {
            assertThat(props.getApiSign().getAlgorithm()).isEqualTo("HMAC-SHA256");
        }

        @Test
        @DisplayName("nonceExpireSeconds 默认应为 300")
        void nonceExpireSecondsShouldBe300() {
            assertThat(props.getApiSign().getNonceExpireSeconds()).isEqualTo(300L);
        }

        @Test
        @DisplayName("timestampToleranceSeconds 默认应为 300")
        void timestampToleranceSecondsShouldBe300() {
            assertThat(props.getApiSign().getTimestampToleranceSeconds()).isEqualTo(300L);
        }
    }

    // ========== 开放平台配置默认值 ==========

    @Nested
    @DisplayName("开放平台配置默认值")
    class OpenPlatformDefaults {

        @Test
        @DisplayName("enabled 默认应为 false")
        void enabledShouldBeFalse() {
            assertThat(props.getOpenPlatform().isEnabled()).isFalse();
        }

        @Test
        @DisplayName("headerAppId 默认应为 X-App-Id")
        void headerAppIdShouldBeXAppId() {
            assertThat(props.getOpenPlatform().getHeaderAppId()).isEqualTo("X-App-Id");
        }

        @Test
        @DisplayName("headerAppSecret 默认应为 X-App-Secret")
        void headerAppSecretShouldBeXAppSecret() {
            assertThat(props.getOpenPlatform().getHeaderAppSecret()).isEqualTo("X-App-Secret");
        }
    }

    // ========== 便捷方法 ==========

    @Nested
    @DisplayName("便捷方法")
    class ConvenienceMethods {

        @Test
        @DisplayName("openPlatform 未启用时应返回 false")
        void isOpenPlatformEnabledShouldBeFalse() {
            assertThat(props.isOpenPlatformEnabled()).isFalse();
        }

        @Test
        @DisplayName("apiSign 未启用时应返回 false")
        void isApiSignEnabledShouldBeFalse() {
            assertThat(props.isApiSignEnabled()).isFalse();
        }
    }

    // ========== Setter ==========

    @Nested
    @DisplayName("属性设置")
    class Setters {

        @Test
        @DisplayName("设置 authMode 应生效")
        void shouldSetAuthMode() {
            props.setAuthMode("oauth2");
            assertThat(props.getAuthMode()).isEqualTo("oauth2");
        }

        @Test
        @DisplayName("设置 anonymousUrls 应生效")
        void shouldSetAnonymousUrls() {
            List<String> urls = List.of("/login", "/register");
            props.setAnonymousUrls(urls);
            assertThat(props.getAnonymousUrls()).containsExactly("/login", "/register");
        }

        @Test
        @DisplayName("设置 tenantEnabled 应生效")
        void shouldEnableTenant() {
            props.setTenantEnabled(true);
            assertThat(props.isTenantEnabled()).isTrue();
        }

        @Test
        @DisplayName("设置 openPlatform enabled 后便捷方法应返回 true")
        void isOpenPlatformEnabledShouldReflectEnabled() {
            props.getOpenPlatform().setEnabled(true);
            assertThat(props.isOpenPlatformEnabled()).isTrue();
        }

        @Test
        @DisplayName("设置 apiSign enabled 后便捷方法应返回 true")
        void isApiSignEnabledShouldReflectEnabled() {
            props.getApiSign().setEnabled(true);
            assertThat(props.isApiSignEnabled()).isTrue();
        }
    }
}
