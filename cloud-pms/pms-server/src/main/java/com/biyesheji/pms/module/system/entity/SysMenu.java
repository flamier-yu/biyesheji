package com.biyesheji.pms.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.biyesheji.pms.common.core.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@Schema(description = "菜单权限")
public class SysMenu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    @Schema(description = "菜单名称")
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "显示顺序")
    private Integer orderNum;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "是否外链(0是 1否)")
    private String isFrame;

    @Schema(description = "是否缓存(0缓存 1不缓存)")
    private String isCache;

    @Schema(description = "菜单类型(M目录 C菜单 F按钮)")
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    @Schema(description = "显示状态(0显示 1隐藏)")
    private String visible;

    @Schema(description = "菜单状态(0正常 1停用)")
    private String status;

    @Schema(description = "权限标识")
    private String perms;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "备注")
    private String remark;

    /* ========== 非表字段 ========== */

    @Schema(description = "子菜单")
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
