package com.basic.log.aspect;

import com.basic.log.annotation.Log;
import com.basic.log.enums.LogStatusEnum;
import com.basic.log.model.LogDO;
import com.basic.log.properties.LogProperties;
import com.basic.log.service.LogService;
import com.basic.log.util.MaskUtil;
import com.basic.log.util.TraceIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 操作日志 AOP 切面
 * <p>
 * 拦截 @Log 注解标记的方法，自动记录操作日志：
 * 1. 记录调用信息（模块、方法、参数、返回值）
 * 2. 对敏感字段进行脱敏处理
 * 3. 记录执行耗时和状态
 * 4. 异步保存操作日志
 * </p>
 *
 * @author actor
 * @date 2024/5/15
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class LogAspect {

    private final LogService logService;
    private final LogProperties logProperties;

    /**
     * 环绕通知：拦截 @Log 注解
     */
    @Around("@annotation(com.basic.log.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);

        // 构建日志实体
        LogDO logDO = new LogDO();
        logDO.setTraceId(TraceIdUtil.getTraceId());
        logDO.setModule(joinPoint.getTarget().getClass().getSimpleName());
        logDO.setOperation(logAnnotation.value());
        logDO.setType(logAnnotation.type().name());
        logDO.setMethod(method.getDeclaringClass().getName() + "." + method.getName());

        // 填充请求上下文信息
        fillRequestInfo(logDO);

        // 记录请求参数（脱敏）
        if (logAnnotation.recordParams()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // 过滤掉 HttpServletRequest、HttpServletResponse 等 Servlet 对象
                Object[] filteredArgs = filterArgs(args);
                if (filteredArgs.length > 0) {
                    if (filteredArgs.length == 1) {
                        logDO.setParams(MaskUtil.toMaskedJson(filteredArgs[0], logProperties));
                    } else {
                        logDO.setParams(MaskUtil.toMaskedJson(filteredArgs, logProperties));
                    }
                }
            }
        }

        // 执行业务方法
        Object result = null;
        try {
            result = joinPoint.proceed();
            logDO.setStatus(LogStatusEnum.SUCCESS.name());
            // 记录返回值
            if (logAnnotation.recordResult() && result != null) {
                // 返回值不进行脱敏，因为返回值通常已是处理后的结果
                try {
                    logDO.setResult(MaskUtil.toMaskedJson(result, logProperties));
                } catch (Exception e) {
                    log.warn("序列化返回值失败: {}", e.getMessage());
                    logDO.setResult("[序列化失败]");
                }
            }
            return result;
        } catch (Throwable e) {
            logDO.setStatus(LogStatusEnum.FAIL.name());
            // 截断过长的异常信息
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 2000) {
                errorMsg = errorMsg.substring(0, 2000) + "...";
            }
            logDO.setErrorMsg(errorMsg);
            throw e;
        } finally {
            // 计算耗时
            long costTime = System.currentTimeMillis() - startTime;
            logDO.setCostTime(costTime);

            // 异步保存日志
            logService.saveLog(logDO);
        }
    }

    /**
     * 填充请求上下文信息（IP、操作人）
     */
    private void fillRequestInfo(LogDO logDO) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logDO.setIp(getClientIp(request));
                // 操作人可以从请求头获取（如 JWT token 中的用户信息）
                String operator = request.getHeader("X-Operator");
                if (operator != null && !operator.isEmpty()) {
                    logDO.setOperator(operator);
                }
            }
        } catch (Exception e) {
            // 非 Web 环境（如定时任务、MQ 消费者）忽略
        }
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个非 unknown 的 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 过滤掉 Servlet 相关参数，避免序列化时循环引用报错
     */
    private Object[] filterArgs(Object[] args) {
        return java.util.Arrays.stream(args)
                .filter(Objects::nonNull)
                .filter(arg -> !(arg instanceof HttpServletRequest)
                        && !(arg instanceof jakarta.servlet.http.HttpServletResponse)
                        && !(arg instanceof jakarta.servlet.ServletRequest)
                        && !(arg instanceof jakarta.servlet.ServletResponse))
                .toArray();
    }
}
