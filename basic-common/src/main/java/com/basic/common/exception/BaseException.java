package com.basic.common.exception;

import cn.hutool.http.HttpStatus;
import lombok.Getter;

/**
 * @Description 业务异常定义类
 * @ClassName BusinessException
 * @Author actor
 * @Date 2022-09-06 11:20
 * @Version 1.0
 */
@Getter
public class BaseException extends RuntimeException {

    private final Integer code;

    public BaseException(String message) {
        super(message);
        this.code = HttpStatus.HTTP_INTERNAL_ERROR;
    }
    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

}
