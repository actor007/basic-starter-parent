package com.basic.log.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceId 工具类
 * <p>
 * 基于 SLF4J MDC 实现链路追踪 ID 的生成、设置、获取和清理。
 * </p>
 *
 * @author actor
 * @date 2024/5/15
 */
public class TraceIdUtil {

    /** MDC 中 traceId 的 key */
    public static final String TRACE_ID_KEY = "traceId";

    /**
     * 生成一个新的 traceId
     *
     * @return UUID 去除横线后的字符串
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 设置 traceId 到 MDC
     *
     * @param traceId traceId
     */
    public static void setTraceId(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * 获取当前 MDC 中的 traceId
     *
     * @return traceId，可能为 null
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 清理 MDC 中的 traceId
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}
