package com.basic.log.service.impl;

import com.alibaba.fastjson.JSON;
import com.basic.log.mapper.LogMapper;
import com.basic.log.model.LogDO;
import com.basic.log.properties.LogProperties;
import com.basic.log.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现
 * <p>
 * 使用 @Async 异步执行，避免阻塞业务主流程。
 * 双写模式：SLF4J 日志输出 + 数据库持久化。
 * </p>
 *
 * @author actor
 * @date 2024/5/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    /** 操作日志专用日志名称 */
    private static final String LOG_NAME = "[OPERATION_LOG]";

    private final LogMapper logMapper;
    private final LogProperties logProperties;

    @Async
    @Override
    public void saveLog(LogDO logDO) {
        // 1. 输出到 SLF4J（控制台 / 日志文件），方便 ELK 采集
        if (logProperties.isSlf4jEnabled()) {
            log.info("{} {}", LOG_NAME, JSON.toJSONString(logDO));
        }

        // 2. 持久化到数据库
        if (logProperties.isDbEnabled()) {
            try {
                logMapper.insert(logDO);
            } catch (Exception e) {
                // 数据库写入失败不应影响业务，仅记录错误日志
                log.error("{} 操作日志写入数据库失败: traceId={}, error={}", LOG_NAME, logDO.getTraceId(), e.getMessage(), e);
            }
        }
    }
}
