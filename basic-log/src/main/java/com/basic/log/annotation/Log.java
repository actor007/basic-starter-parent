package com.basic.log.annotation;

import com.basic.log.enums.LogTypeEnum;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * <p>
 * 标记在 Controller 或 Service 方法上，通过 AOP 自动记录操作日志。
 * 支持双写：SLF4J 日志文件 + 数据库持久化，由配置开关控制。
 * </p>
 *
 * @author actor
 * @date 2024/5/15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作描述，例如："新增用户"、"删除订单"
     */
    String value() default "";

    /**
     * 操作类型
     */
    LogTypeEnum type() default LogTypeEnum.OTHER;

    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录返回值（大数据量场景建议关闭）
     */
    boolean recordResult() default false;
}
