package com.biyesheji.pms.common.exception;

import com.biyesheji.pms.common.enums.ResultCode;

/**
 * 业务异常
 * <p>
 * 业务校验失败时抛出，由 GlobalExceptionHandler 统一转为响应体，不打印完整堆栈。
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;

    public ServiceException() {
        super();
        this.code = ResultCode.ERROR.getCode();
    }

    public ServiceException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
    }

    public ServiceException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public ServiceException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public ServiceException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public Integer getCode() {
        return code;
    }
}
