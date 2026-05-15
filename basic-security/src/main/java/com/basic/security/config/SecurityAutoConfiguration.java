package com.basic.security.config;

import com.basic.security.api.ApiSignFilter;
import com.basic.security.api.ApiSignUtil;
import com.basic.security.captcha.CaptchaFilter;
import com.basic.security.captcha.CaptchaService;
import com.basic.security.core.properties.SecurityProperties;
import com.basic.security.jwt.JwtAuthenticationFilter;
import com.basic.security.jwt.JwtUtil;
import com.basic.security.jwt.TokenService;
import com.basic.security.openplatform.OpenPlatformAuthFilter;
import com.basic.security.tenant.TenantFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Security 模块自动配置类
 * <p>
 * 按条件装配 JWT、Tenant、Captcha、OpenPlatform、ApiSign 等过滤器。
 *
 * @author actor
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class SecurityAutoConfiguration {

    private final SecurityProperties securityProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== JWT ====================

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "basic.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public JwtUtil jwtUtil() {
        return new JwtUtil(securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "basic.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public TokenService tokenService(JwtUtil jwtUtil) {
        return new TokenService(jwtUtil, securityProperties, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "basic.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtUtil jwtUtil) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthenticationFilter(jwtUtil, securityProperties));
        registration.addUrlPatterns("/*");
        registration.setOrder(-90);
        registration.setName("jwtAuthenticationFilter");
        log.info("JWT filter registered");
        return registration;
    }

    // ==================== Tenant ====================

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "basic.security.tenant.enabled", havingValue = "true")
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration() {
        FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantFilter(securityProperties));
        registration.addUrlPatterns("/*");
        registration.setOrder(-80);
        registration.setName("tenantFilter");
        log.info("Tenant filter registered");
        return registration;
    }

    // ==================== Captcha ====================

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "basic.security.captcha.enabled", havingValue = "true")
    public CaptchaService captchaService() {
        return new CaptchaService(securityProperties, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "basic.security.captcha.enabled", havingValue = "true")
    public FilterRegistrationBean<CaptchaFilter> captchaFilterRegistration(CaptchaService captchaService) {
        FilterRegistrationBean<CaptchaFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CaptchaFilter(captchaService, securityProperties));
        registration.addUrlPatterns("/*");
        registration.setOrder(-70);
        registration.setName("captchaFilter");
        log.info("Captcha filter registered");
        return registration;
    }

    // ==================== OpenPlatform ====================

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "basic.security.open-platform.enabled", havingValue = "true")
    public FilterRegistrationBean<OpenPlatformAuthFilter> openPlatformFilterRegistration() {
        FilterRegistrationBean<OpenPlatformAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OpenPlatformAuthFilter(securityProperties, redisTemplate));
        registration.addUrlPatterns("/*");
        registration.setOrder(-60);
        registration.setName("openPlatformAuthFilter");
        log.info("OpenPlatform filter registered");
        return registration;
    }

    // ==================== ApiSign ====================

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "basic.security.api-sign.enabled", havingValue = "true")
    public ApiSignUtil apiSignUtil() {
        return new ApiSignUtil(redisTemplate, securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "basic.security.api-sign.enabled", havingValue = "true")
    public FilterRegistrationBean<ApiSignFilter> apiSignFilterRegistration(ApiSignUtil apiSignUtil) {
        FilterRegistrationBean<ApiSignFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiSignFilter(apiSignUtil, securityProperties));
        registration.addUrlPatterns("/*");
        registration.setOrder(-50);
        registration.setName("apiSignFilter");
        log.info("API sign filter registered");
        return registration;
    }

}
