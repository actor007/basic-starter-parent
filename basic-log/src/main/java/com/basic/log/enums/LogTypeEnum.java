package com.basic.log.enums;

/**
 * 操作日志类型枚举
 *
 * @author actor
 * @date 2024/5/15
 */
public enum LogTypeEnum {

    /** 新增 */
    INSERT,
    /** 修改 */
    UPDATE,
    /** 删除 */
    DELETE,
    /** 查询 */
    QUERY,
    /** 导出 */
    EXPORT,
    /** 导入 */
    IMPORT,
    /** 登录 */
    LOGIN,
    /** 登出 */
    LOGOUT,
    /** 其他 */
    OTHER
}
