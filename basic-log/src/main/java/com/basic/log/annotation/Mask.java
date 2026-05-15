package com.basic.log.annotation;

import java.lang.annotation.*;

/**
 * 敏感字段脱敏注解
 * <p>
 * 标记在 DTO/VO 字段上，标识该字段为敏感信息，
 * 在记录日志时自动进行脱敏处理（如 password、mobile 等）。
 * </p>
 *
 * @author actor
 * @date 2024/5/15
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mask {

    /**
     * 脱敏策略
     */
    MaskType type() default MaskType.DEFAULT;

    /**
     * 脱敏策略枚举
     */
    enum MaskType {
        /** 默认：保留首尾字符，中间替换为 **** */
        DEFAULT,
        /** 全隐藏：全部替换为 **** */
        ALL,
        /** 仅保留后4位 */
        LAST_4,
        /** 仅保留前3位 */
        FIRST_3,
        /** 邮箱：隐藏 @ 前用户名部分 */
        EMAIL
    }
}
