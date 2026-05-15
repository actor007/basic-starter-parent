package com.basic.log.filter;

import com.basic.log.util.TraceIdUtil;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TraceId 过滤器
 * <p>
 * 在请求进入时从请求头获取或生成 TraceId，设置到 MDC 中，
 * 并在响应头中返回 TraceId 供调用方/前端进行链路追踪。
 * </p>
 *
 * @author actor
 * @date 2024/5/15
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    /** 请求头中 traceId 的 key */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 尝试从请求头获取 traceId
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceIdUtil.generateTraceId();
        }

        // 2. 设置到 MDC
        TraceIdUtil.setTraceId(traceId);

        // 3. 将 traceId 返回给调用方
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 4. 请求结束后清理 MDC，防止内存泄漏
            MDC.clear();
        }
    }
}
