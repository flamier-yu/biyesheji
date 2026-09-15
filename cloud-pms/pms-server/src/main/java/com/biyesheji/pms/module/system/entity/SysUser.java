package com.biyesheji.pms.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.biyesheji.pms.common.core.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "用户信息")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "登录账号")
    @NotBlank(message = "登录账号不能为空")
    @Size(max = 30, message = "登录账号长度不能超过30个字符")
    private String username;

    @Schema(description = "用户昵称")
    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickName;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    @Schema(description = "手机号")
    @Size(max = 11, message = "手机号长度不能超过11个字符")
    private String phonenumber;

    @Schema(description = "性别(0男 1女 2未知)")
    private String sex;

    @Schema(description = "头像地址")
    private String avatar;

    /** 密码：入参可写，出参不返回 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(description = "状态(0正常 1停用)")
    private String status;

    /** 逻辑删除标志：不对外暴露 */
    @JsonIgnore
    private String delFlag;

    @Schema(description = "最后登录IP")
    private String loginIp;

    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginDate;

    @Schema(description = "备注")
    private String remark;

    /* ========== 以下为非表字段 ========== */

    @Schema(description = "部门名称")
    @TableField(exist = false)
    private String deptName;

    @Schema(description = "角色ID数组")
    @TableField(exist = false)
    private Long[] roleIds;

    @Schema(description = "岗位ID数组")
    @TableField(exist = false)
    private Long[] postIds;
}
