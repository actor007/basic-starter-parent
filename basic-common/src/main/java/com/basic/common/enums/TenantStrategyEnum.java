package com.basic.common.enums;

/**
 * 多租户隔离策略枚举
 *
 * @author actor
 */
public enum TenantStrategyEnum {

    /**
     * 行级隔离：通过 tenant_id 列过滤，共享同一张表
     */
    DISCRIMINATOR,

    /**
     * Schema级隔离：每个租户独立Schema
     */
    SCHEMA,

    /**
     * 数据库级隔离：每个租户独立数据库
     */
    DATABASE

}
