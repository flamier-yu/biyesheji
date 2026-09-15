package com.biyesheji.pms.module.system.controller;

import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.system.entity.SysRole;
import com.biyesheji.pms.module.system.service.ISysRoleService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色管理
 */
@Tag(name = "04. 角色管理", description = "角色增删改查与菜单权限分配")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
@Validated
public class SysRoleController {

    private final ISysRoleService roleService;

    @Operation(summary = "分页查询角色列表")
    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/list")
    public R<PageResult<SysRole>> list(SysRole role,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(roleService.selectRolePage(role, pageNum, pageSize));
    }

    @Operation(summary = "查询全部可用角色（下拉用）")
    @GetMapping("/all")
    public R<List<SysRole>> all() {
        return R.ok(roleService.selectRoleAll());
    }

    @Operation(summary = "查询角色详情（含已分配菜单）")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/{roleId}")
    public R<SysRole> getInfo(@PathVariable Long roleId) {
        SysRole role = roleService.getById(roleId);
        if (role != null) {
            role.setMenuIds(roleService.selectMenuIdsByRoleId(roleId).toArray(new Long[0]));
        }
        return R.ok(role);
    }

    @Operation(summary = "新增角色")
    @PreAuthorize("hasAuthority('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysRole role) {
        return R.toAjax(roleService.insertRole(role));
    }

    @Operation(summary = "修改角色")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysRole role) {
        return R.toAjax(roleService.updateRole(role));
    }

    @Operation(summary = "删除角色（支持批量）")
    @PreAuthorize("hasAuthority('system:role:remove')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable Long[] roleIds) {
        return R.toAjax(roleService.deleteRoleByIds(roleIds));
    }

    @Operation(summary = "启用/停用角色")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysRole role) {
        return R.toAjax(roleService.changeStatus(role.getRoleId(), role.getStatus()));
    }
}
