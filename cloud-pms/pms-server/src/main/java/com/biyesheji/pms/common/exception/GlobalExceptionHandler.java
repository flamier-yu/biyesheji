package com.biyesheji.pms.common.exception;

import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：属于预期内的失败，只记 warn，不打堆栈
     */
    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.warn("业务异常 [{}] {} -> {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        Integer code = e.getCode() == null ? ResultCode.ERROR.getCode() : e.getCode();
        return R.fail(code, e.getMessage());
    }

    /**
     * 权限不足
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限不足 [{}] {}", request.getMethod(), request.getRequestURI());
        return R.fail(ResultCode.FORBIDDEN);
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(AuthenticationException.class)
    public R<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败 [{}] {} -> {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return R.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * @Valid 校验失败（RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", msg);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * @Valid 校验失败（表单）
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数绑定失败：{}", msg);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 单参数校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", msg);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 缺少必填参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.fail(ResultCode.BAD_REQUEST.getCode(), "缺少必要参数：" + e.getParameterName());
    }

    /**
     * 请求体解析失败
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return R.fail(ResultCode.BAD_REQUEST.getCode(), "请求参数格式不正确");
    }

    /**
     * 请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持：{} {}", request.getMethod(), request.getRequestURI());
        return R.fail(ResultCode.METHOD_NOT_ALLOWED);
    }

    /**
     * 文件超限
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return R.fail(ResultCode.FILE_SIZE_EXCEED);
    }

    /**
     * 唯一索引冲突
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("数据唯一性冲突：{}", e.getMessage());
        return R.fail(ResultCode.CONFLICT);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(ResultCode.ERROR);
    }
}
