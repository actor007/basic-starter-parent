package com.basic.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.basic.dal.handler.TenantMetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 多租户 DAL 层自动配置
 * <p>
 * 通过 basic.tenant.enabled=true 开启多租户拦截器。
 * 可配置 basic.tenant.ignore-tables 忽略特定表。
 *
 * @author actor
 */
@Slf4j
@Configuration
@ConditionalOnClass(MybatisPlusInterceptor.class)
@ConditionalOnProperty(prefix = "basic.tenant", name = "enabled", havingValue = "true")
public class TenantDalAutoConfiguration {

    /**
     * 默认租户行处理器
     * <p>
     * 可通过 basic.tenant.tenant-column 指定租户列名，默认为 tenant_id。
     * 可通过 basic.tenant.ignore-tables 指定忽略的表，多个用逗号分隔。
     */
    @Bean
    @ConditionalOnMissingBean(TenantLineHandler.class)
    public TenantLineHandler defaultTenantLineHandler(
            @Value("${basic.tenant.tenant-column:tenant_id}") String tenantColumn,
            @Value("${basic.tenant.ignore-tables:}") List<String> ignoreTables) {

        Set<String> ignoreTableSet = new HashSet<>(ignoreTables);
        log.info("Tenant interceptor enabled: tenantColumn={}, ignoreTables={}", tenantColumn, ignoreTableSet);
        return com.basic.dal.interceptor.TenantLineInterceptorFactory.createDefaultHandler(tenantColumn, ignoreTableSet);
    }

    /**
     * 多租户行级隔离拦截器 Bean
     */
    @Bean
    @ConditionalOnMissingBean(TenantLineInnerInterceptor.class)
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantLineHandler handler) {
        return new TenantLineInnerInterceptor(handler);
    }

    /**
     * 租户元数据自动填充
     */
    @Bean
    @ConditionalOnMissingBean(TenantMetaObjectHandler.class)
    public TenantMetaObjectHandler tenantMetaObjectHandler() {
        return new TenantMetaObjectHandler();
    }
}
