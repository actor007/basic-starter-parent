package com.basic.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 (Knife4j) 配置
 *
 * @author actor
 * @since 3.0
 */
@Configuration
@ConditionalOnProperty(name = "springfox.basePackage")
public class SpringFoxConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 配置 OpenAPI 基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("在线文档")
                        .version("1.0.0")
                        .description("# 对外公开的接口简介")
                        .contact(new Contact()
                                .name("北京弘源泰")
                                .url("http://www.hyt.com/")
                                .email("mail@hyt.com")));
    }
}
