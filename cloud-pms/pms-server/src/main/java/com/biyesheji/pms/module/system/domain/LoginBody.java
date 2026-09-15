package com.biyesheji.pms.module.system.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录请求参数
 */
@Data
@Schema(description = "登录请求参数")
public class LoginBody implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "登录账号", example = "admin")
    @NotBlank(message = "登录账号不能为空")
    private String username;

    @Schema(description = "登录密码", example = "admin123")
    @NotBlank(message = "登录密码不能为空")
    private String password;

    @Schema(description = "验证码")
    private String code;

    @Schema(description = "验证码唯一标识")
    private String uuid;
}
