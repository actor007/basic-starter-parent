package com.basic.security.captcha;

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
import java.util.List;

/**
 * 验证码校验过滤器
 * <p>
 * 对配置中需要验证码的 URL 进行拦截校验。
 *
 * @author actor
 */
@Slf4j
@RequiredArgsConstructor
public class CaptchaFilter extends OncePerRequestFilter {

    private final CaptchaService captchaService;
    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String captchaId = request.getHeader("X-Captcha-Id");
        String captchaCode = request.getHeader("X-Captcha-Code");
        if (StringUtils.hasText(captchaId) && StringUtils.hasText(captchaCode)) {
            if (!captchaService.verify(captchaId, captchaCode)) {
                throw new SecurityException.CaptchaException("验证码错误或已过期");
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        List<String> captchaUrls = securityProperties.getCaptcha().getUrls();
        if (captchaUrls == null || captchaUrls.isEmpty()) return true;
        return captchaUrls.stream().noneMatch(pattern -> {
            if (pattern.endsWith("/**")) {
                return request.getRequestURI().startsWith(pattern.substring(0, pattern.length() - 3));
            }
            return pattern.equals(request.getRequestURI());
        });
    }
}
