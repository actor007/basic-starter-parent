package com.basic.log.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.basic.dal.base.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志实体
 * <p>
 * 继承 {@link BaseDO}，自动拥有 id、create_time、modify_time、deleted、version 字段。
 * 对应数据库表 t_operation_log。
 * </p>
 *
 * @author actor
 * @date 2024/5/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_operation_log")
public class LogDO extends BaseDO {

    /** 链路追踪 ID */
    private String traceId;

    /** 模块名（通常为 Controller 类名） */
    private String module;

    /** 操作描述 */
    private String operation;

    /** 操作类型 */
    private String type;

    /** 方法全路径（类名.方法名） */
    private String method;

    /** 请求参数（JSON 序列化，敏感字段已脱敏） */
    private String params;

    /** 返回结果（JSON 序列化） */
    private String result;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 操作人 */
    private String operator;

    /** 请求 IP */
    private String ip;

    /** 执行状态：SUCCESS / FAIL */
    private String status;

    /** 异常信息（仅失败时记录） */
    private String errorMsg;
}
