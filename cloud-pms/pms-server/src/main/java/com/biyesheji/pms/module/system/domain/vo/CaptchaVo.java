package com.biyesheji.pms.module.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 验证码信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证码信息")
public class CaptchaVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "验证码唯一标识，登录时需原样回传")
    private String uuid;

    @Schema(description = "验证码图片（Base64，可直接用于 img 标签）")
    private String img;

    @Schema(description = "是否开启验证码")
    private Boolean captchaEnabled;
}
