package com.basic.common.context;

import com.basic.common.enums.TenantStrategyEnum;

/**
 * 多租户上下文持有者
 * <p>
 * 使用 InheritableThreadLocal 以支持父子线程间的上下文传递。
 * 线程池场景建议配合 alibaba transmittable-thread-local 使用。
 *
 * @author actor
 */
public class TenantContextHolder {

    private static final ThreadLocal<String> TENANT_ID_HOLDER = new InheritableThreadLocal<>();
    private static final ThreadLocal<TenantStrategyEnum> STRATEGY_HOLDER = new InheritableThreadLocal<>();

    /**
     * 设置当前租户ID
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
    }

    /**
     * 获取当前租户ID
     */
    public static String getTenantId() {
        return TENANT_ID_HOLDER.get();
    }

    /**
     * 设置当前租户隔离策略
     */
    public static void setStrategy(TenantStrategyEnum strategy) {
        STRATEGY_HOLDER.set(strategy);
    }

    /**
     * 获取当前租户隔离策略
     */
    public static TenantStrategyEnum getStrategy() {
        return STRATEGY_HOLDER.get();
    }

    /**
     * 清除上下文（防止内存泄漏，务必在请求结束后调用）
     */
    public static void clear() {
        TENANT_ID_HOLDER.remove();
        STRATEGY_HOLDER.remove();
    }

}
