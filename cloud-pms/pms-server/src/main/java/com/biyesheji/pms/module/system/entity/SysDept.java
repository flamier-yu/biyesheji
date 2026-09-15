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

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
@Schema(description = "部门信息")
public class SysDept extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "部门ID")
    @TableId(value = "dept_id", type = IdType.AUTO)
    private Long deptId;

    @Schema(description = "父部门ID")
    @NotNull(message = "上级部门不能为空")
    private Long parentId;

    @Schema(description = "祖级列表")
    private String ancestors;

    @Schema(description = "部门名称")
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过50个字符")
    private String deptName;

    @Schema(description = "显示顺序")
    @NotNull(message = "显示顺序不能为空")
    private Integer orderNum;

    @Schema(description = "负责人")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "状态(0正常 1停用)")
    private String status;

    @JsonIgnore
    private String delFlag;

    /* ========== 非表字段 ========== */

    @Schema(description = "上级部门名称")
    @TableField(exist = false)
    private String parentName;

    @Schema(description = "子部门")
    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();
}
