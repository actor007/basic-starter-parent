package com.basic.security.annotation;

import java.lang.annotation.*;

/**
 * 匿名访问注解
 * <p>
 * 标记在 Controller 方法上，表示无需认证即可访问。
 *
 * @author actor
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnonymousAccess {
}
