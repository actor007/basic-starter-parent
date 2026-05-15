package com.basic.security.core.exception;

import com.basic.common.exception.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Security 模块基础异常，继承 common 的 BaseException，
 * 由 {@link com.basic.common.handler.GlobalExceptionHandler} 统一拦截处理。
 *
 * @author actor
 */
public class SecurityException extends BaseException {

    public SecurityException(int code, String message) {
        super(code, message);
    }

    public SecurityException(int code, String message, Throwable cause) {
        super(code, message);
    }

    // ========== 子异常类（通过 @ResponseStatus 声明 HTTP 状态码） ==========

    /**
     * 未登录 / Token 无效 → 401
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class UnauthorizedException extends SecurityException {
        public UnauthorizedException(String message) {
            super(401, message);
        }
        public UnauthorizedException(String message, Throwable cause) {
            super(401, message);
        }
    }

    /**
     * 无权限 → 403
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class ForbiddenException extends SecurityException {
        public ForbiddenException(String message) {
            super(403, message);
        }
        public ForbiddenException(String message, Throwable cause) {
            super(403, message);
        }
    }

    /**
     * Token 过期 → 401
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class TokenExpiredException extends SecurityException {
        public TokenExpiredException(String message) {
            super(40101, message);
        }
        public TokenExpiredException(String message, Throwable cause) {
            super(40101, message);
        }
    }

    /**
     * 验证码错误 → 400
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class CaptchaException extends SecurityException {
        public CaptchaException(String message) {
            super(40001, message);
        }
        public CaptchaException(String message, Throwable cause) {
            super(40001, message);
        }
    }

    /**
     * API 签名校验失败 → 400
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ApiSignException extends SecurityException {
        public ApiSignException(String message) {
            super(40002, message);
        }
        public ApiSignException(String message, Throwable cause) {
            super(40002, message);
        }
    }

    /**
     * 租户异常 → 400
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class TenantException extends SecurityException {
        public TenantException(String message) {
            super(40003, message);
        }
        public TenantException(String message, Throwable cause) {
            super(40003, message);
        }
    }

    /**
     * 开放平台异常 → 400
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class OpenPlatformException extends SecurityException {
        public OpenPlatformException(String message) {
            super(40004, message);
        }
        public OpenPlatformException(String message, Throwable cause) {
            super(40004, message);
        }
    }

}
