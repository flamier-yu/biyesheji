package com.biyesheji.pms.module.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 当前登录用户信息
 */
@Data
@Schema(description = "当前登录用户信息")
public class UserInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "角色标识集合")
    private Set<String> roles;

    @Schema(description = "权限标识集合")
    private Set<String> permissions;
}
