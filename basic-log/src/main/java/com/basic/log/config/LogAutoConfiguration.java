package com.basic.log.config;

import com.basic.log.aspect.LogAspect;
import com.basic.log.properties.LogProperties;
import com.basic.log.service.LogService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 日志模块自动配置
 * <p>
 * 通过 spring.factories 自动装配，启用：
 * 1. @EnableAsync — 异步日志写入
 * 2. @MapperScan — 扫描 LogMapper
 * 3. LogProperties — 配置属性绑定
 * 4. LogAspect — AOP 切面
 * </p>
 *
 * @author actor
 * @date 2024/5/15
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(LogProperties.class)
@MapperScan("com.basic.log.mapper")
public class LogAutoConfiguration {

    /**
     * 注册 LogAspect Bean
     */
    @Bean
    public LogAspect logAspect(LogService logService, LogProperties logProperties) {
        return new LogAspect(logService, logProperties);
    }
}
