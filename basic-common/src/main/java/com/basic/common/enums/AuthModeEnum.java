package com.basic.common.enums;

/**
 * 认证模式枚举
 *
 * @author actor
 */
public enum AuthModeEnum {

    /**
     * JWT无状态认证
     */
    JWT,

    /**
     * OAuth2认证（Spring Authorization Server）
     */
    OAUTH2,

    /**
     * Session + 表单登录
     */
    SESSION,

    /**
     * 开放平台 AppKey/AppSecret 模式
     */
    OPEN_PLATFORM,

    /**
     * API签名模式
     */
    API_SIGN

}
