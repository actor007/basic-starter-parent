package com.basic.common.handler;

import com.basic.common.base.Result;
import com.basic.common.enums.ErrorStatusEnum;
import com.basic.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author actor
 * @date 2019-05-22
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnClass(HttpServletRequest.class)
public class GlobalExceptionHandler {

	/**
	 * 全局异常.
	 *
	 * @param e the e
	 * @return R
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Result exception(Exception e) {
		if (log.isErrorEnabled()) {
			log.error("==> 全局异常信息：", e);
		}
		return Result.error(ErrorStatusEnum.SYSTEM_ERROR);
	}

	/**
	 * 参数格式异常处理
	 */
	@ExceptionHandler({IllegalArgumentException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result badRequestException(IllegalArgumentException ex) {
		if (log.isErrorEnabled()) {
			log.error("==> 参数格式不合法：{}", ex.getMessage());
		}
		return Result.error(ErrorStatusEnum.ILLEGALARGUMENT_ERROR);
	}

	/**
	 * 参数缺失异常处理
	 */
	@ExceptionHandler({MissingServletRequestParameterException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result badRequestException(Exception ex) {
		if (log.isErrorEnabled()) {
			log.error("==> 缺少必填参数：{}", ex.getMessage());
		}
		return Result.error(String.valueOf(HttpStatus.BAD_REQUEST.value()), "缺少必填参数！");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Result httpMessageNotReadableException(HttpMessageNotReadableException e) {
		if (log.isErrorEnabled()) {
			log.error("==> 缺少请求主体或反序列化失败:{}", e.getMessage(), e.fillInStackTrace());
		}
		return Result.error(ErrorStatusEnum.SERIALIZABLE_ERROR);
	}

	@ExceptionHandler(NullPointerException.class)
	@ResponseStatus(HttpStatus.OK)
	public Result nullPointerException(NullPointerException e) {
		if (log.isErrorEnabled()) {
			log.error("==> 空指针异常：{}", e.getMessage(), e);
		}
		return Result.error(ErrorStatusEnum.NULLPOINTER_ERROR);
	}

	/**
	 * 处理@Valid校验的数据
	 *
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = {MethodArgumentNotValidException.class})
	public Result MethodArgumentNotValidException(MethodArgumentNotValidException e) {
		BindingResult rs = e.getBindingResult();
		StringBuilder resultMsg = new StringBuilder();
		if (rs.hasErrors()) {
			List<FieldError> fieldErrors = rs.getFieldErrors();
			fieldErrors.forEach(fieldError -> {
				resultMsg.append(fieldError.getDefaultMessage()).append(",");
				if (log.isErrorEnabled()) {
					log.error("==> error field is : {} ,message is : {}", fieldError.getField(), fieldError.getDefaultMessage());
				}
			});
		}
		return Result.error(resultMsg.deleteCharAt(resultMsg.length() - 1).toString());
	}

	/**
	 * 普通的参数传递的形式;
	 *
	 * @param req
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(value = BindException.class)
	@ResponseBody
	public Result validatedGetException(HttpServletRequest req, BindException exception) {
		String errorMsg = "";
		if (exception.hasErrors()) {
			List<FieldError> fieldErrors = exception.getFieldErrors();
			List<String> errorList = fieldErrors.stream().map(e -> e.getDefaultMessage()).collect(Collectors.toList());
			errorMsg = errorList.stream().map(String::valueOf).collect(Collectors.joining(","));
		}
		return Result.error(errorMsg);
	}

	/**
	 * 处理@NotBlank校验的数据
	 *
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = {ConstraintViolationException.class})
	public Result ConstraintViolationException(ConstraintViolationException e) {
		String excMsg = e.getMessage();
		String name = excMsg.substring(0, excMsg.indexOf("."));
		String msg = excMsg.substring(excMsg.indexOf(":") + 1);
		String fieldName = excMsg.substring(excMsg.indexOf(".") + 1, excMsg.indexOf(":"));
		if (log.isErrorEnabled()) {
			log.error("==> interface : {}; error field is : {} ,message is : {}", name, fieldName, msg);
		}
		return Result.error(msg);
	}

	@ExceptionHandler(value = BaseException.class)
	public Result taxReportBusinessExceptionHandle(BaseException baseException) {
		return Result.error(String.valueOf(baseException.getCode()), baseException.getMessage());
	}

}
