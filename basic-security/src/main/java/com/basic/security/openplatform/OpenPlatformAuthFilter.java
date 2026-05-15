package com.basic.security.openplatform;

import com.basic.security.core.constant.SecurityConstant;
import com.basic.security.core.exception.SecurityException;
import com.basic.security.core.properties.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 开放平台认证过滤器
 * <p>
 * 校验 AppId + AppSecret + Timestamp 签名，防止重放。
 *
 * @author actor
 */
@Slf4j
@RequiredArgsConstructor
public class OpenPlatformAuthFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final long NONCE_WINDOW_MS = 5 * 60 * 1000; // 5 分钟窗口

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String appId = request.getHeader(SecurityConstant.APP_ID_HEADER);
        String timestamp = request.getHeader(SecurityConstant.TIMESTAMP_HEADER);
        String nonce = request.getHeader(SecurityConstant.NONCE_HEADER);
        String sign = request.getHeader(SecurityConstant.SIGN_HEADER);

        if (!StringUtils.hasText(appId) || !StringUtils.hasText(timestamp)
                || !StringUtils.hasText(nonce) || !StringUtils.hasText(sign)) {
            throw new SecurityException.OpenPlatformException("缺少必要的认证参数");
        }

        // 校验时间戳
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new SecurityException.OpenPlatformException("时间戳格式错误");
        }
        if (Math.abs(System.currentTimeMillis() - ts) > NONCE_WINDOW_MS) {
            throw new SecurityException.OpenPlatformException("请求已过期");
        }

        // Nonce 防重放（Redis）
        String nonceKey = SecurityConstant.REDIS_OPENPLATFORM_NONCE_KEY + nonce;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(nonceKey, "1", NONCE_WINDOW_MS, TimeUnit.MILLISECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new SecurityException.OpenPlatformException("重复请求");
        }

        // 从 Redis 获取 AppSecret
        String secretKey = SecurityConstant.REDIS_OPENPLATFORM_SECRET_KEY + appId;
        Object secretObj = redisTemplate.opsForValue().get(secretKey);
        if (secretObj == null) {
            throw new SecurityException.OpenPlatformException("无效的AppId");
        }
        String appSecret = secretObj.toString();

        // 验证签名：MD5(appId + appSecret + timestamp + nonce)
        String expectSign = DigestUtils.md5DigestAsHex(
                (appId + appSecret + timestamp + nonce).getBytes(StandardCharsets.UTF_8));
        if (!expectSign.equals(sign)) {
            throw new SecurityException.OpenPlatformException("签名校验失败");
        }

        log.debug("OpenPlatform auth OK: appId={}", appId);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !securityProperties.isOpenPlatformEnabled() ||
                securityProperties.getOpenPlatform().getUrls().stream().noneMatch(p ->
                        request.getRequestURI().startsWith(p.replace("/**", "")));
    }
}
