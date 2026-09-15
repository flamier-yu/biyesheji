package com.biyesheji.pms.module.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 前端动态路由对象
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "前端路由")
public class RouterVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "路由名称")
    private String name;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "是否隐藏")
    private Boolean hidden;

    @Schema(description = "重定向地址")
    private String redirect;

    @Schema(description = "组件地址")
    private String component;

    @Schema(description = "是否始终显示根菜单")
    private Boolean alwaysShow;

    @Schema(description = "路由元信息")
    private MetaVo meta;

    @Schema(description = "子路由")
    private List<RouterVo> children;
}
