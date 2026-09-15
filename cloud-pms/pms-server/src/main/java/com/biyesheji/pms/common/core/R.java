package com.biyesheji.pms.common.core;

import com.biyesheji.pms.common.enums.ResultCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体
 */
@Data
@Schema(description = "统一响应结果")
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码，200 表示成功")
    private Integer code;

    @Schema(description = "提示信息")
    private String msg;

    @Schema(description = "返回数据")
    private T data;

    public R() {
    }

    public R(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> R<T> fail() {
        return new R<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMsg(), null);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(ResultCode.ERROR.getCode(), msg, null);
    }

    public static <T> R<T> fail(Integer code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    /**
     * 根据影响行数返回结果，常用于增删改
     */
    public static R<Void> toAjax(int rows) {
        return rows > 0 ? R.ok() : R.fail("操作失败，影响行数为 0");
    }

    public static R<Void> toAjax(boolean result) {
        return result ? R.ok() : R.fail("操作失败");
    }
}
