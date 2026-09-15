package com.biyesheji.pms.module.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 前端路由元信息
 */
@Data
@Schema(description = "路由元信息")
public class MetaVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单名称")
    private String title;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "是否不缓存")
    private Boolean noCache;

    @Schema(description = "外链地址")
    private String link;

    public MetaVo() {
    }

    public MetaVo(String title, String icon, Boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
    }

    public MetaVo(String title, String icon, Boolean noCache, String link) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        this.link = link;
    }
}
