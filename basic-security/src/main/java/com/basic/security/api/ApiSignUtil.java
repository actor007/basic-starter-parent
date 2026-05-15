package com.basic.security.api;

import com.basic.security.core.constant.SecurityConstant;
import com.basic.security.core.exception.SecurityException;
import com.basic.security.core.properties.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * API 签名工具类
 * <p>
 * 支持 HMAC-SHA256、MD5、RSA 三种签名方式 + Nonce 防重放。
 *
 * @author actor
 */
@Slf4j
@RequiredArgsConstructor
public class ApiSignUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecurityProperties securityProperties;

    /** Nonce 有效期 5 分钟 */
    private static final long NONCE_WINDOW_MS = 5 * 60 * 1000;

    // ==================== 签名生成 ====================

    public String signHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec spec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new SecurityException.ApiSignException("HMAC-SHA256签名失败", e);
        }
    }

    public String signMd5(String data, String secret) {
        return DigestUtils.md5DigestAsHex((data + secret).getBytes(StandardCharsets.UTF_8));
    }

    public String signRsa(String data, String privateKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(spec);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new SecurityException.ApiSignException("RSA签名失败", e);
        }
    }

    // ==================== 签名验证 ====================

    public boolean verifyHmacSha256(String data, String secret, String sign) {
        return signHmacSha256(data, secret).equals(sign);
    }

    public boolean verifyMd5(String data, String secret, String sign) {
        return signMd5(data, secret).equals(sign);
    }

    public boolean verifyRsa(String data, String publicKeyStr, String sign) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(spec);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            throw new SecurityException.ApiSignException("RSA验签失败", e);
        }
    }

    // ==================== Nonce 防重放 ====================

    public void checkNonce(String appId, String nonce) {
        String key = SecurityConstant.REDIS_API_NONCE_KEY + appId + ":" + nonce;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, "1", NONCE_WINDOW_MS, TimeUnit.MILLISECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new SecurityException.ApiSignException("重复请求");
        }
    }

}
