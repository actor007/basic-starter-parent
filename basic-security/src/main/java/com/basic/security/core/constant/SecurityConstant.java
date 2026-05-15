package com.basic.security.core.constant;

/**
 * Security 常量定义
 *
 * @author actor
 */
public final class SecurityConstant {

    private SecurityConstant() {
    }

    // ========== 配置前缀 ==========
    public static final String CONFIG_PREFIX = "basic.security";

    // ========== Token 相关 ==========
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    // ========== Redis Key 前缀 ==========
    public static final String REDIS_TOKEN_KEY = "basic:security:token:";
    public static final String REDIS_REFRESH_TOKEN_KEY = "basic:security:refresh:";
    public static final String REDIS_CAPTCHA_KEY = "basic:security:captcha:";
    public static final String REDIS_API_SIGN_NONCE = "basic:security:api:nonce:";
    public static final String REDIS_OAUTH2_CLIENT = "basic:security:oauth2:client:";
    public static final String REDIS_OPENPLATFORM_APP = "basic:security:openplatform:app:";

    // ========== 多租户 ==========
    public static final String TENANT_HEADER = "X-Tenant-Id";
    public static final String TENANT_DEFAULT_COLUMN = "tenant_id";

    // ========== 开放平台 / API 签名 Header ==========
    public static final String APP_ID_HEADER = "X-App-Id";
    public static final String APP_SECRET_HEADER = "X-App-Secret";
    public static final String TIMESTAMP_HEADER = "X-Timestamp";
    public static final String NONCE_HEADER = "X-Nonce";
    public static final String SIGN_HEADER = "X-Sign";
    public static final String SIGN_TYPE_HEADER = "X-Sign-Type";

    // ========== 开放平台 / API 签名 Redis Key ==========
    public static final String REDIS_API_NONCE_KEY = "basic:security:api:nonce:";
    public static final String REDIS_OPENPLATFORM_NONCE_KEY = "basic:security:openplatform:nonce:";
    public static final String REDIS_OPENPLATFORM_SECRET_KEY = "basic:security:openplatform:secret:";

    // ========== 默认值 ==========
    public static final String DEFAULT_ADMIN_ROLE = "ROLE_ADMIN";
    public static final String DEFAULT_USER_ROLE = "ROLE_USER";

    // ========== 用户类型 ==========
    public static final String USER_TYPE_ADMIN = "admin";
    public static final String USER_TYPE_NORMAL = "normal";
    public static final String USER_TYPE_OPEN = "open";

}
