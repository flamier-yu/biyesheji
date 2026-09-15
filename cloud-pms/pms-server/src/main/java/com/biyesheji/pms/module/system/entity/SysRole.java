package com.biyesheji.pms.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.biyesheji.pms.common.core.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "角色信息")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID")
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过30个字符")
    private String roleName;

    @Schema(description = "角色权限字符串")
    @NotBlank(message = "权限字符不能为空")
    @Size(max = 100, message = "权限字符长度不能超过100个字符")
    private String roleKey;

    @Schema(description = "显示顺序")
    @NotNull(message = "显示顺序不能为空")
    private Integer roleSort;

    @Schema(description = "数据范围(1全部 2本部门 3本部门及以下 4仅本人)")
    private String dataScope;

    @Schema(description = "状态(0正常 1停用)")
    private String status;

    @JsonIgnore
    private String delFlag;

    @Schema(description = "备注")
    private String remark;

    /* ========== 非表字段 ========== */

    @Schema(description = "菜单ID数组")
    @TableField(exist = false)
    private Long[] menuIds;
}
