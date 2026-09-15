package com.biyesheji.pms.module.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录结果")
public class LoginVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "访问令牌")
    private String token;
}
