package com.basic.security.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Security 统一配置属性
 *
 * @author actor
 */
@Data
@ConfigurationProperties(prefix = "basic.security")
public class SecurityProperties {

    /**
     * 认证模式：jwt / oauth2 / session
     */
    private String authMode = "jwt";

    /**
     * 匿名访问路径（白名单）
     */
    private List<String> anonymousUrls = new ArrayList<>();

    /**
     * 是否开启多租户
     */
    private boolean tenantEnabled = false;

    /**
     * JWT 配置
     */
    private JwtProperties jwt = new JwtProperties();

    /**
     * 验证码配置
     */
    private CaptchaProperties captcha = new CaptchaProperties();

    /**
     * API 签名配置
     */
    private ApiSignProperties apiSign = new ApiSignProperties();

    /**
     * 开放平台配置
     */
    private OpenPlatformProperties openPlatform = new OpenPlatformProperties();

    @Data
    public static class JwtProperties {
        private String secret = "BasicSecretKey2024ChangeMeInProduction";
        private long expiration = 86400000L; // 24h
        private long refreshExpiration = 604800000L; // 7d
        private String tokenHeader = "Authorization";
        private String tokenPrefix = "Bearer ";
    }

    @Data
    public static class CaptchaProperties {
        private boolean enabled = false;
        private String type = "arithmetic"; // arithmetic / gif
        private long expireSeconds = 120L;
        private int width = 130;
        private int height = 48;
        private String store = "memory"; // memory / redis
        private List<String> urls = new ArrayList<>();
    }

    @Data
    public static class ApiSignProperties {
        private boolean enabled = false;
        private String algorithm = "HMAC-SHA256"; // HMAC-SHA256 / MD5 / RSA
        private long nonceExpireSeconds = 300L;
        private long timestampToleranceSeconds = 300L;
        private java.util.Map<String, String> appSecrets = new java.util.HashMap<>();
    }

    @Data
    public static class OpenPlatformProperties {
        private boolean enabled = false;
        private String headerAppId = "X-App-Id";
        private String headerAppSecret = "X-App-Secret";
        private List<String> urls = new ArrayList<>();
    }

    // ==================== 便捷方法 ====================

    public boolean isOpenPlatformEnabled() {
        return openPlatform != null && openPlatform.enabled;
    }

    public boolean isApiSignEnabled() {
        return apiSign != null && apiSign.enabled;
    }

}
