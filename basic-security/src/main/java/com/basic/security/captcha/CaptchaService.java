package com.basic.security.captcha;

import com.basic.security.core.constant.SecurityConstant;
import com.basic.security.core.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 * <p>
 * 支持算术验证码，存储方式：内存 / Redis。
 *
 * @author actor
 */
@Slf4j
@RequiredArgsConstructor
public class CaptchaService {

    private final SecurityProperties securityProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 内存存储（Fallback 当 Redis 不可用时）
     */
    private final ConcurrentHashMap<String, CaptchaEntry> memoryStore = new ConcurrentHashMap<>();

    /**
     * 生成算术验证码
     */
    public CaptchaResult generate() {
        int width = securityProperties.getCaptcha().getWidth();
        int height = securityProperties.getCaptcha().getHeight();

        // 生成算术表达式
        int num1 = (int) (Math.random() * 20) + 1;
        int num2 = (int) (Math.random() * 20) + 1;
        int operatorIndex = (int) (Math.random() * 3); // 0:+, 1:-, 2:*
        char operator;
        int result;
        String expression;

        switch (operatorIndex) {
            case 0 -> { operator = '+'; result = num1 + num2; }
            case 1 -> { operator = '-'; result = num1 - num2; }
            default -> { operator = '*'; result = num1 * num2; }
        }
        expression = num1 + " " + operator + " " + num2 + " = ?";

        // 生成图片
        BufferedImage image = createImage(width, height, expression);
        String base64Image;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate captcha image", e);
        }

        // 存储验证码
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String value = String.valueOf(result);
        saveCaptcha(captchaId, value);

        return new CaptchaResult(captchaId, "data:image/png;base64," + base64Image);
    }

    /**
     * 校验验证码
     */
    public boolean verify(String captchaId, String input) {
        String stored = getCaptcha(captchaId);
        if (stored == null) return false;
        boolean match = stored.equals(input);
        if (match) removeCaptcha(captchaId);
        return match;
    }

    /**
     * 保存验证码
     */
    private void saveCaptcha(String id, String value) {
        long expire = securityProperties.getCaptcha().getExpireSeconds();
        if ("redis".equalsIgnoreCase(securityProperties.getCaptcha().getStore())) {
            try {
                redisTemplate.opsForValue().set(
                        SecurityConstant.REDIS_CAPTCHA_KEY + id, value, expire, TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                log.warn("Redis captcha save failed, fallback to memory: {}", e.getMessage());
            }
        }
        memoryStore.put(id, new CaptchaEntry(value, System.currentTimeMillis() + expire * 1000));
    }

    private String getCaptcha(String id) {
        if ("redis".equalsIgnoreCase(securityProperties.getCaptcha().getStore())) {
            try {
                Object val = redisTemplate.opsForValue().get(SecurityConstant.REDIS_CAPTCHA_KEY + id);
                return val != null ? val.toString() : null;
            } catch (Exception e) {
                log.warn("Redis captcha get failed, fallback to memory: {}", e.getMessage());
            }
        }
        CaptchaEntry entry = memoryStore.get(id);
        if (entry != null && entry.expireTime > System.currentTimeMillis()) return entry.value;
        memoryStore.remove(id);
        return null;
    }

    private void removeCaptcha(String id) {
        if ("redis".equalsIgnoreCase(securityProperties.getCaptcha().getStore())) {
            try {
                redisTemplate.delete(SecurityConstant.REDIS_CAPTCHA_KEY + id);
                return;
            } catch (Exception e) { /* silent */ }
        }
        memoryStore.remove(id);
    }

    private BufferedImage createImage(int width, int height, String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 背景
        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, width, height);
        // 干扰线
        g.setColor(new Color(180, 180, 180));
        for (int i = 0; i < 4; i++) {
            int x1 = (int) (Math.random() * width);
            int y1 = (int) (Math.random() * height);
            int x2 = (int) (Math.random() * width);
            int y2 = (int) (Math.random() * height);
            g.drawLine(x1, y1, x2, y2);
        }
        // 文字
        g.setColor(new Color(50, 100, 180));
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString(text, 10, 32);
        g.dispose();
        return image;
    }

    public record CaptchaResult(String captchaId, String base64Image) {}
    private record CaptchaEntry(String value, long expireTime) {}

}
