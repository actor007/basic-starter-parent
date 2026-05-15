package com.basic.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈响应code码枚举〉
 *
 * @author actor
 * @create 2022/9/12
 */
@Getter
@AllArgsConstructor
public enum ErrorStatusEnum {

	TRANSACTION_ERROR("5000", "事务异常！"),
	ILLEGALARGUMENT_ERROR("5000", "参数格式不合法！"),
	NULLPOINTER_ERROR("5000", "空指针异常！"),
	SERIALIZABLE_ERROR("5000", "缺少请求主体或反序列化失败！"),
	SYSTEM_ERROR("5000", "系统错误，请联系管理员！");

	private final String code;

	private final String message;

}
