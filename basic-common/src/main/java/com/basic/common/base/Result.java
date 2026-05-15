package com.basic.common.base;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @packageName: com.basic.common.base
 * @className: Result.java
 * @author: actor
 * @description: 响应信息主体
 * @date: 2022/8/20
 */
@Getter
@Setter
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1755124163846889347L;
    public static final String SUCCESS_CODE = "200";
    public static final String ERORR_CODE = "500";
    public static final String SUCCESS = "操作成功";

    /**
     * 返回信息
     */
    private String msg = "操作成功";
    /**
     * 返回消息类型,0：成功
     */
    private String code = "2000";

    private T result;

    public Result() {
    }

    private Result(String code, String msg, T result) {
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    /**
     * 构建返回结果
     *
     * @param msg
     * @param code
     * @return
     */
    public static <T> Result<T> build(String code, String msg) {
        return new Result(code, msg, "");
    }

    /**
     * 构建返回结果
     *
     * @param msg
     * @param code
     * @param result
     * @return
     */
    public static <T> Result<T> build(String code, String msg, T result) {
        return new Result(code, msg, result);
    }

    /**
     * 构建返回结果，code默认值为200
     *
     * @param msg
     * @param result
     * @return
     */
    public static <T> Result<T> build(String msg, T result) {
        return build(SUCCESS_CODE, msg, result);
    }

    /**
     * 构建成功结果
     *
     * @param msg
     * @param result
     * @return
     */
    public static <T> Result<T> success(String msg, T result) {
        return build(msg, result);
    }

    public static <T> Result<T> success(String code, String msg, T result) {
        return build(code, msg, result);
    }


    /**
     * 构建成功结果带信息
     *
     * @return
     */
    public static <T> Result<T> success() {
        return success(SUCCESS, null);
    }

    /**
     * 构建成功结果待数据
     *
     * @param result
     * @return
     */
    public static <T> Result<T> success(T result) {
        return success(null, result);
    }


    /**
     * 构建失败结果待数据
     *
     * @param msg
     * @return
     */
    public static <T> Result<T> error(String msg) {
        return error(ERORR_CODE, msg);
    }

    /**
     * 构建失败结果待数据
     *
     * @param code
     * @param msg
     * @return
     */
    public static <T> Result<T> error(String code, String msg) {
        return build(code, msg, null);
    }

    /**
     * 构建失败结果
     *
     * @param msg
     * @param result
     * @return
     */
    public static <T> Result<T> error(String msg, T result) {
        return error(ERORR_CODE, msg, result);
    }

    /**
     * 构建失败结果
     *
     * @param msg
     * @param result
     * @return
     */
    public static <T> Result<T> error(String code, String msg, T result) {
        return build(code, msg, result);
    }

    /**
     * 构建失败结果带数据
     *
     * @param result
     * @return
     */
    public static <T> Result<T> error(T result) {
        return error("", result);
    }

    /**
     * 构建失败结果带默认值
     *
     * @return
     */
    public static <T> Result<T> error() {
        return error("500", "系统错误，请联系管理员！");
    }
}
