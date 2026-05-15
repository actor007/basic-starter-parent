package com.basic.security.api;

import com.basic.security.core.properties.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ApiSignUtil 单元测试
 *
 * @author actor
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ApiSignUtil API签名工具测试")
class ApiSignUtilTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private SecurityProperties securityProperties;
    private ApiSignUtil apiSignUtil;

    private static final String SECRET = "test-secret-key-2024";
    private static final String DATA = "appId=test&timestamp=1700000000&nonce=abc123";

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        apiSignUtil = new ApiSignUtil(redisTemplate, securityProperties);
    }

    // ========== HMAC-SHA256 ==========

    @Nested
    @DisplayName("HMAC-SHA256 签名和验签")
    class HmacSha256Tests {

        @Test
        @DisplayName("应生成非空签名")
        void shouldGenerateNonNullSignature() {
            String sign = apiSignUtil.signHmacSha256(DATA, SECRET);
            assertThat(sign).isNotBlank();
        }

        @Test
        @DisplayName("相同数据与密钥应产生相同签名")
        void shouldBeDeterministic() {
            String sign1 = apiSignUtil.signHmacSha256(DATA, SECRET);
            String sign2 = apiSignUtil.signHmacSha256(DATA, SECRET);
            assertThat(sign1).isEqualTo(sign2);
        }

        @Test
        @DisplayName("不同数据应产生不同签名")
        void shouldDifferForDifferentData() {
            String sign1 = apiSignUtil.signHmacSha256("data1", SECRET);
            String sign2 = apiSignUtil.signHmacSha256("data2", SECRET);
            assertThat(sign1).isNotEqualTo(sign2);
        }

        @Test
        @DisplayName("不同密钥应产生不同签名")
        void shouldDifferForDifferentSecret() {
            String sign1 = apiSignUtil.signHmacSha256(DATA, "secret1");
            String sign2 = apiSignUtil.signHmacSha256(DATA, "secret2");
            assertThat(sign1).isNotEqualTo(sign2);
        }

        @Test
        @DisplayName("应能正确验签")
        void shouldVerifyCorrectly() {
            String sign = apiSignUtil.signHmacSha256(DATA, SECRET);
            assertThat(apiSignUtil.verifyHmacSha256(DATA, SECRET, sign)).isTrue();
        }

        @Test
        @DisplayName("错误签名应判定无效")
        void shouldRejectWrongSignature() {
            assertThat(apiSignUtil.verifyHmacSha256(DATA, SECRET, "wrong-sign")).isFalse();
        }

        @Test
        @DisplayName("签名应为 Base64 编码")
        void signatureShouldBeBase64() {
            String sign = apiSignUtil.signHmacSha256(DATA, SECRET);
            // Base64 decoding should not throw
            Base64.getDecoder().decode(sign);
        }
    }

    // ========== MD5 ==========

    @Nested
    @DisplayName("MD5 签名和验签")
    class Md5Tests {

        @Test
        @DisplayName("应生成非空 MD5 签名")
        void shouldGenerateNonNullSignature() {
            String sign = apiSignUtil.signMd5(DATA, SECRET);
            assertThat(sign).isNotBlank();
        }

        @Test
        @DisplayName("MD5 签名应为 32 位十六进制")
        void shouldBe32HexChars() {
            String sign = apiSignUtil.signMd5(DATA, SECRET);
            assertThat(sign).hasSize(32);
            assertThat(sign).matches("[0-9a-f]{32}");
        }

        @Test
        @DisplayName("相同输入应产生相同签名")
        void shouldBeDeterministic() {
            String sign1 = apiSignUtil.signMd5(DATA, SECRET);
            String sign2 = apiSignUtil.signMd5(DATA, SECRET);
            assertThat(sign1).isEqualTo(sign2);
        }

        @Test
        @DisplayName("应能正确验签")
        void shouldVerifyCorrectly() {
            String sign = apiSignUtil.signMd5(DATA, SECRET);
            assertThat(apiSignUtil.verifyMd5(DATA, SECRET, sign)).isTrue();
        }

        @Test
        @DisplayName("错误签名应判定无效")
        void shouldRejectWrongSignature() {
            assertThat(apiSignUtil.verifyMd5(DATA, SECRET, "wrong-sign")).isFalse();
        }
    }

    // ========== RSA ==========

    @Nested
    @DisplayName("RSA 签名和验签")
    class RsaTests {

        private String privateKeyStr;
        private String publicKeyStr;

        @BeforeEach
        void setUp() throws Exception {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            privateKeyStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            publicKeyStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        }

        @Test
        @DisplayName("应生成非空 RSA 签名")
        void shouldGenerateNonNullSignature() {
            String sign = apiSignUtil.signRsa(DATA, privateKeyStr);
            assertThat(sign).isNotBlank();
        }

        @Test
        @DisplayName("应能正确验签")
        void shouldVerifyCorrectly() {
            String sign = apiSignUtil.signRsa(DATA, privateKeyStr);
            assertThat(apiSignUtil.verifyRsa(DATA, publicKeyStr, sign)).isTrue();
        }

        @Test
        @DisplayName("篡改数据后验签应失败")
        void shouldRejectTamperedData() {
            String sign = apiSignUtil.signRsa(DATA, privateKeyStr);
            assertThat(apiSignUtil.verifyRsa("tampered-data", publicKeyStr, sign)).isFalse();
        }

        @Test
        @DisplayName("错误公钥验签应失败")
        void shouldRejectWrongPublicKey() throws Exception {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            String otherPublicKey = Base64.getEncoder().encodeToString(keyGen.generateKeyPair().getPublic().getEncoded());

            String sign = apiSignUtil.signRsa(DATA, privateKeyStr);
            assertThat(apiSignUtil.verifyRsa(DATA, otherPublicKey, sign)).isFalse();
        }
    }

    // ========== Nonce 防重放 ==========

    @Nested
    @DisplayName("Nonce 防重放")
    class NonceTests {

        @Test
        @DisplayName("首次 nonce 应通过")
        void shouldPassForFirstNonce() {
            when(valueOperations.setIfAbsent(anyString(), eq("1"), anyLong(), eq(TimeUnit.MILLISECONDS)))
                    .thenReturn(true);

            // Should not throw
            apiSignUtil.checkNonce("app-001", "nonce-001");
            verify(valueOperations).setIfAbsent(contains("app-001:nonce-001"), eq("1"), anyLong(), eq(TimeUnit.MILLISECONDS));
        }

        @Test
        @DisplayName("重复 nonce 应抛出 ApiSignException")
        void shouldThrowForDuplicateNonce() {
            when(valueOperations.setIfAbsent(anyString(), eq("1"), anyLong(), eq(TimeUnit.MILLISECONDS)))
                    .thenReturn(false);

            assertThatThrownBy(() -> apiSignUtil.checkNonce("app-001", "nonce-001"))
                    .isInstanceOf(com.basic.security.core.exception.SecurityException.ApiSignException.class)
                    .hasMessageContaining("重复请求");
        }
    }
}
