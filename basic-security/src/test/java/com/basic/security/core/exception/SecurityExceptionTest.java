package com.basic.security.core.exception;

import com.basic.common.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SecurityException 单元测试
 *
 * @author actor
 */
@DisplayName("SecurityException 异常体系测试")
class SecurityExceptionTest {

    // ========== 基础特性 ==========

    @Test
    @DisplayName("应继承 BaseException")
    void shouldExtendBaseException() {
        SecurityException ex = new SecurityException(500, "test");
        assertThat(ex).isInstanceOf(BaseException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("应正确保存 code 和 message")
    void shouldStoreCodeAndMessage() {
        SecurityException ex = new SecurityException(40300, "禁止访问");
        assertThat(ex.getCode()).isEqualTo(40300);
        assertThat(ex.getMessage()).isEqualTo("禁止访问");
    }

    @Test
    @DisplayName("code 应继承自父类")
    void codeShouldBeInherited() {
        SecurityException ex = new SecurityException(999, "msg");
        assertThat(ex.getCode()).isEqualTo(999);
    }

    // ========== UnauthorizedException ==========

    @Test
    @DisplayName("UnauthorizedException code 应为 401")
    void unauthorizedShouldHaveCode401() {
        SecurityException.UnauthorizedException ex = new SecurityException.UnauthorizedException("未登录");
        assertThat(ex.getCode()).isEqualTo(401);
        assertThat(ex.getMessage()).isEqualTo("未登录");
    }

    // ========== ForbiddenException ==========

    @Test
    @DisplayName("ForbiddenException code 应为 403")
    void forbiddenShouldHaveCode403() {
        SecurityException.ForbiddenException ex = new SecurityException.ForbiddenException("无权限");
        assertThat(ex.getCode()).isEqualTo(403);
    }

    // ========== TokenExpiredException ==========

    @Test
    @DisplayName("TokenExpiredException code 应为 40101")
    void tokenExpiredShouldHaveCode40101() {
        SecurityException.TokenExpiredException ex = new SecurityException.TokenExpiredException("Token过期");
        assertThat(ex.getCode()).isEqualTo(40101);
    }

    // ========== CaptchaException ==========

    @Test
    @DisplayName("CaptchaException code 应为 40001")
    void captchaShouldHaveCode40001() {
        SecurityException.CaptchaException ex = new SecurityException.CaptchaException("验证码错误");
        assertThat(ex.getCode()).isEqualTo(40001);
    }

    // ========== ApiSignException ==========

    @Test
    @DisplayName("ApiSignException code 应为 40002")
    void apiSignShouldHaveCode40002() {
        SecurityException.ApiSignException ex = new SecurityException.ApiSignException("签名失败");
        assertThat(ex.getCode()).isEqualTo(40002);
    }

    // ========== TenantException ==========

    @Test
    @DisplayName("TenantException code 应为 40003")
    void tenantShouldHaveCode40003() {
        SecurityException.TenantException ex = new SecurityException.TenantException("租户无效");
        assertThat(ex.getCode()).isEqualTo(40003);
    }

    // ========== OpenPlatformException ==========

    @Test
    @DisplayName("OpenPlatformException code 应为 40004")
    void openPlatformShouldHaveCode40004() {
        SecurityException.OpenPlatformException ex = new SecurityException.OpenPlatformException("开放平台异常");
        assertThat(ex.getCode()).isEqualTo(40004);
    }

    // ========== 异常捕获兼容性 ==========

    @Test
    @DisplayName("子异常应可被 BaseException 捕获")
    void childExceptionShouldBeCatchableAsBaseException() {
        assertThatThrownBy(() -> {
            throw new SecurityException.UnauthorizedException("test");
        }).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("子异常应可被 SecurityException 捕获")
    void childExceptionShouldBeCatchableAsSecurityException() {
        assertThatThrownBy(() -> {
            throw new SecurityException.ForbiddenException("test");
        }).isInstanceOf(SecurityException.class);
    }
}
