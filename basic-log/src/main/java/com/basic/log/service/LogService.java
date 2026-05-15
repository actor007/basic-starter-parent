package com.basic.log.service;

import com.basic.log.model.LogDO;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务接口
 *
 * @author actor
 * @date 2024/5/15
 */
public interface LogService {

    /**
     * 异步保存操作日志
     * <p>
     * 支持双写模式：
     * 1. 通过 SLF4J 输出到日志文件/控制台
     * 2. 持久化到数据库
     * 具体行为由 basic.log.db-enabled 和 basic.log.slf4j-enabled 配置控制。
     * </p>
     *
     * @param logDO 操作日志实体
     */
    void saveLog(LogDO logDO);
}
