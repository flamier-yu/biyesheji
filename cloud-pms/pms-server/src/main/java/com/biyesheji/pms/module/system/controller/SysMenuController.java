package com.biyesheji.pms.module.system.controller;

import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.system.entity.SysMenu;
import com.biyesheji.pms.module.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 菜单管理
 */
@Tag(name = "05. 菜单管理", description = "菜单增删改查与权限标识维护")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
@Validated
public class SysMenuController {

    private final ISysMenuService menuService;

    @Operation(summary = "查询菜单树")
    @PreAuthorize("hasAuthority('system:menu:list')")
    @GetMapping("/list")
    public R<List<SysMenu>> list(SysMenu menu) {
        return R.ok(menuService.selectAllMenuTree());
    }

    @Operation(summary = "查询菜单下拉树（供角色授权使用，含按钮）")
    @GetMapping("/treeselect")
    public R<List<SysMenu>> treeSelect() {
        return R.ok(menuService.selectAllMenuTree());
    }

    @Operation(summary = "查询菜单详情")
    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/{menuId}")
    public R<SysMenu> getInfo(@PathVariable Long menuId) {
        return R.ok(menuService.getById(menuId));
    }

    @Operation(summary = "新增菜单")
    @PreAuthorize("hasAuthority('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysMenu menu) {
        return R.toAjax(menuService.insertMenu(menu));
    }

    @Operation(summary = "修改菜单")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysMenu menu) {
        return R.toAjax(menuService.updateMenu(menu));
    }

    @Operation(summary = "删除菜单")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public R<Void> remove(@PathVariable Long menuId) {
        return R.toAjax(menuService.deleteMenuById(menuId));
    }
}
