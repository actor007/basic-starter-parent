package com.basic.security.api;

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
 * API 签名校验过滤器
 *
 * @author actor
 */
@Slf4j
@RequiredArgsConstructor
public class ApiSignFilter extends OncePerRequestFilter {

    private final ApiSignUtil apiSignUtil;
    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String appId = request.getHeader(SecurityConstant.APP_ID_HEADER);
        String timestamp = request.getHeader(SecurityConstant.TIMESTAMP_HEADER);
        String nonce = request.getHeader(SecurityConstant.NONCE_HEADER);
        String sign = request.getHeader(SecurityConstant.SIGN_HEADER);
        String signType = request.getHeader(SecurityConstant.SIGN_TYPE_HEADER);

        if (!StringUtils.hasText(appId) || !StringUtils.hasText(timestamp)
                || !StringUtils.hasText(nonce) || !StringUtils.hasText(sign)) {
            throw new SecurityException.ApiSignException("缺少签名参数");
        }

        // 时间戳校验
        long ts;
        try { ts = Long.parseLong(timestamp); } catch (NumberFormatException e) {
            throw new SecurityException.ApiSignException("时间戳格式错误");
        }
        if (Math.abs(System.currentTimeMillis() - ts) > 5 * 60 * 1000) {
            throw new SecurityException.ApiSignException("请求已过期");
        }

        // Nonce 防重放
        apiSignUtil.checkNonce(appId, nonce);

        // 签名数据: appId + timestamp + nonce + (body hash)
        String data = appId + timestamp + nonce;

        boolean verified;
        String type = StringUtils.hasText(signType) ? signType : "md5";
        switch (type.toLowerCase()) {
            case "hmac-sha256" -> verified = apiSignUtil.verifyHmacSha256(data, getSecret(appId), sign);
            case "rsa" -> verified = apiSignUtil.verifyRsa(data, getSecret(appId), sign);
            default -> verified = apiSignUtil.verifyMd5(data, getSecret(appId), sign);
        }

        if (!verified) {
            throw new SecurityException.ApiSignException("签名校验失败");
        }

        log.debug("API sign verified OK: appId={}", appId);
        filterChain.doFilter(request, response);
    }

    private String getSecret(String appId) {
        // 从配置或 DB 获取 secret
        return securityProperties.getApiSign().getAppSecrets() != null
                ? securityProperties.getApiSign().getAppSecrets().getOrDefault(appId, appId)
                : appId;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !securityProperties.isApiSignEnabled();
    }
}
