package com.basic.dal.interceptor;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.basic.common.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 多租户行级隔离拦截器 - 工厂
 * <p>
 * 提供默认 TenantLineHandler 实现。
 * 使用方式：在自动配置或 Bean 定义中注册 MyBatis-Plus 原生的
 * com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor。
 *
 * @author actor
 */
@Slf4j
public final class TenantLineInterceptorFactory {

    private TenantLineInterceptorFactory() {}

    /**
     * 创建默认租户行处理器：从 TenantContextHolder 读取当前租户ID
     *
     * @param tenantColumn 租户字段名
     * @param ignoreTables 忽略表集合
     * @return TenantLineHandler
     */
    public static TenantLineHandler createDefaultHandler(String tenantColumn, Set<String> ignoreTables) {
        return new CustomTenantLineHandler(tenantColumn, ignoreTables);
    }

    @Slf4j
    public static class CustomTenantLineHandler implements TenantLineHandler {

        private final String tenantColumn;
        private final Set<String> ignoreTables;

        public CustomTenantLineHandler(String tenantColumn, Set<String> ignoreTables) {
            this.tenantColumn = tenantColumn;
            this.ignoreTables = ignoreTables != null ? new HashSet<>(ignoreTables) : new HashSet<>();
        }

        public void addIgnoreTable(String... tableNames) {
            ignoreTables.addAll(Arrays.asList(tableNames));
        }

        @Override
        public Expression getTenantId() {
            String tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                return null;
            }
            return new StringValue(tenantId);
        }

        @Override
        public String getTenantIdColumn() {
            return tenantColumn;
        }

        @Override
        public boolean ignoreTable(String tableName) {
            return ignoreTables.contains(tableName);
        }

        @Override
        public boolean ignoreInsert(List columns, String tenantIdColumn) {
            return true;
        }
    }
}
