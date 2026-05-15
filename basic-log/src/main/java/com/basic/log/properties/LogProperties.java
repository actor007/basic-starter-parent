package com.basic.log.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志模块配置属性
 * <p>
 * 在 application.yml 中通过 basic.log 前缀进行配置。
 * </p>
 *
 * <pre>
 * basic:
 *   log:
 *     db-enabled: true
 *     slf4j-enabled: true
 *     mask-enabled: true
 *     mask-fields:
 *       - password
 *       - mobile
 * </pre>
 *
 * @author actor
 * @date 2024/5/15
 */
@Data
@ConfigurationProperties(prefix = "basic.log")
public class LogProperties {

    /** 是否启用数据库存储，默认 true */
    private boolean dbEnabled = true;

    /** 是否输出到 SLF4J 日志（控制台/文件），默认 true */
    private boolean slf4jEnabled = true;

    /** 是否启用敏感数据脱敏，默认 true */
    private boolean maskEnabled = true;

    /** 全局敏感字段名列表（不依赖 @Mask 注解的场景） */
    private List<String> maskFields = new ArrayList<>();
}
