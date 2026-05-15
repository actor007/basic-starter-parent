package com.basic.security.tenant;

import com.basic.common.context.TenantContextHolder;
import com.basic.security.core.constant.SecurityConstant;
import com.basic.security.core.exception.SecurityException;
import com.basic.security.core.properties.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 多租户过滤器
 * <p>
 * 从请求头中提取 X-Tenant-Id 并设置到 TenantContextHolder。
 *
 * @author actor
 */
@Slf4j
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (securityProperties.isTenantEnabled()) {
            String tenantId = request.getHeader(SecurityConstant.TENANT_HEADER);
            if (StringUtils.hasText(tenantId)) {
                TenantContextHolder.setTenantId(tenantId);
                log.debug("Tenant filter set tenantId: {}", tenantId);
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    TenantContextHolder.clear();
                }
                return;
            } else {
                log.warn("Tenant filter: missing tenant header for path: {}", request.getRequestURI());
                throw new SecurityException.TenantException("缺少租户标识");
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 白名单路径不校验租户
        return securityProperties.getAnonymousUrls().stream()
                .anyMatch(pattern -> {
                    if (pattern.endsWith("/**")) {
                        return request.getRequestURI().startsWith(pattern.substring(0, pattern.length() - 3));
                    }
                    return pattern.equals(request.getRequestURI());
                });
    }
}
