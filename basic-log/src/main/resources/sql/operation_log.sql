-- =====================================================
-- 操作日志表
-- 对应实体：com.basic.log.model.LogDO
-- =====================================================

CREATE TABLE IF NOT EXISTS `t_operation_log` (
    `id`            BIGINT          NOT NULL COMMENT '主键ID（雪花算法）',
    `trace_id`      VARCHAR(64)     DEFAULT NULL COMMENT '链路追踪ID',
    `module`        VARCHAR(128)    DEFAULT NULL COMMENT '模块名（Controller/Service类名）',
    `operation`     VARCHAR(256)    DEFAULT NULL COMMENT '操作描述',
    `type`          VARCHAR(32)     DEFAULT NULL COMMENT '操作类型：INSERT/UPDATE/DELETE/QUERY/EXPORT/IMPORT/LOGIN/LOGOUT/OTHER',
    `method`        VARCHAR(512)    DEFAULT NULL COMMENT '方法全路径（包名.类名.方法名）',
    `params`        TEXT            DEFAULT NULL COMMENT '请求参数（JSON，敏感字段已脱敏）',
    `result`        TEXT            DEFAULT NULL COMMENT '返回结果（JSON）',
    `cost_time`     BIGINT          DEFAULT NULL COMMENT '执行耗时（毫秒）',
    `operator`      VARCHAR(64)     DEFAULT NULL COMMENT '操作人',
    `ip`            VARCHAR(64)     DEFAULT NULL COMMENT '请求IP',
    `status`        VARCHAR(16)     DEFAULT NULL COMMENT '执行状态：SUCCESS / FAIL',
    `error_msg`     TEXT            DEFAULT NULL COMMENT '异常信息（仅失败时记录）',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       INT             DEFAULT 0 COMMENT '逻辑删除标记（0=正常，1=删除）',
    `version`       INT             DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    INDEX `idx_trace_id` (`trace_id`),
    INDEX `idx_module` (`module`),
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_operator` (`operator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';
