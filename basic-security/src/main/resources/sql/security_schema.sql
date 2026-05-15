-- =====================================================
-- basic-security 建表脚本
-- =====================================================

-- OAuth2 客户端授权表
CREATE TABLE IF NOT EXISTS `oauth2_client` (
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `client_id`   VARCHAR(64)  NOT NULL COMMENT '客户端ID',
    `client_secret` VARCHAR(128) NOT NULL COMMENT '客户端密钥',
    `client_name` VARCHAR(128) DEFAULT NULL COMMENT '客户端名称',
    `scopes`      VARCHAR(256) DEFAULT 'read' COMMENT '授权范围',
    `grant_types` VARCHAR(256) DEFAULT 'password,refresh_token,authorization_code' COMMENT '授权类型',
    `redirect_uri` VARCHAR(512) DEFAULT NULL COMMENT '回调地址',
    `access_token_validity`   INT DEFAULT 3600 COMMENT 'AccessToken有效期(秒)',
    `refresh_token_validity`  INT DEFAULT 86400 COMMENT 'RefreshToken有效期(秒)',
    `auto_approve`   TINYINT(1) DEFAULT 1 COMMENT '是否自动批准',
    `status`         TINYINT(1) DEFAULT 1 COMMENT '状态:0禁用,1启用',
    `create_time`    DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2客户端配置表';

-- 开放平台 App 管理表
CREATE TABLE IF NOT EXISTS `open_platform_app` (
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `app_id`      VARCHAR(64)  NOT NULL COMMENT '应用ID',
    `app_name`    VARCHAR(128) NOT NULL COMMENT '应用名称',
    `app_secret`  VARCHAR(256) NOT NULL COMMENT '应用密钥',
    `status`      TINYINT(1)   DEFAULT 1 COMMENT '状态:0禁用,1启用',
    `remark`      VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台App表';

-- 租户配置表（可选，若持久化租户信息）
CREATE TABLE IF NOT EXISTS `tenant_config` (
    `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id`     VARCHAR(64)  NOT NULL COMMENT '租户ID',
    `tenant_name`   VARCHAR(128) NOT NULL COMMENT '租户名称',
    `status`        TINYINT(1)   DEFAULT 1 COMMENT '状态:0禁用,1启用',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户配置表';
