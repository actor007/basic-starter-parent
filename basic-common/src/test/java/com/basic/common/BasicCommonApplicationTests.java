package com.basic.common;

import com.basic.common.base.Result;
import com.basic.common.enums.AuthModeEnum;
import com.basic.common.enums.ErrorStatusEnum;
import com.basic.common.enums.TenantStrategyEnum;
import com.basic.common.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * basic-common 基础功能测试
 */
class BasicCommonApplicationTests {

    @Test
    @DisplayName("Result - success without data")
    void testResultSuccessNoData() {
        Result<String> result = Result.success();
        assertEquals("200", result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertNull(result.getResult());
    }

    @Test
    @DisplayName("Result - success with data")
    void testResultSuccessWithData() {
        Result<String> result = Result.success("hello");
        assertEquals("200", result.getCode());
        assertEquals("hello", result.getResult());
    }

    @Test
    @DisplayName("Result - error with message")
    void testResultErrorWithMsg() {
        Result<String> result = Result.error("参数错误");
        assertEquals("500", result.getCode());
        assertEquals("参数错误", result.getMsg());
    }

    @Test
    @DisplayName("Result - error with code and message")
    void testResultErrorWithCodeAndMsg() {
        Result<String> result = Result.error("400", "参数错误");
        assertEquals("400", result.getCode());
        assertEquals("参数错误", result.getMsg());
    }

    @Test
    @DisplayName("BaseException - custom message")
    void testBaseException() {
        BaseException ex = new BaseException(500, "自定义异常");
        assertEquals(500, ex.getCode());
        assertEquals("自定义异常", ex.getMessage());
    }

    @Test
    @DisplayName("BaseException - default code 500")
    void testBaseExceptionDefaultCode() {
        BaseException ex = new BaseException("系统错误");
        assertEquals(500, ex.getCode());
        assertEquals("系统错误", ex.getMessage());
    }

    @Test
    @DisplayName("ErrorStatusEnum - all values defined")
    void testErrorStatusEnumValues() {
        ErrorStatusEnum[] values = ErrorStatusEnum.values();
        assertEquals(5, values.length);
        for (ErrorStatusEnum e : values) {
            assertNotNull(e.getCode());
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("ErrorStatusEnum - SYSTEM_ERROR")
    void testErrorStatusEnumSystemError() {
        assertEquals("5000", ErrorStatusEnum.SYSTEM_ERROR.getCode());
        assertEquals("系统错误，请联系管理员！", ErrorStatusEnum.SYSTEM_ERROR.getMessage());
    }

    @Test
    @DisplayName("AuthModeEnum - all values defined")
    void testAuthModeEnum() {
        AuthModeEnum[] values = AuthModeEnum.values();
        assertTrue(values.length > 0);
    }

    @Test
    @DisplayName("TenantStrategyEnum - all values defined")
    void testTenantStrategyEnum() {
        TenantStrategyEnum[] values = TenantStrategyEnum.values();
        assertTrue(values.length > 0);
    }
}
