package com.biyesheji.pms.module.system.controller;

import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.system.entity.SysUser;
import com.biyesheji.pms.module.system.service.ISysUserService;
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
 * 用户管理
 */
@Tag(name = "03. 用户管理", description = "用户增删改查、重置密码、启用停用")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Validated
public class SysUserController {

    private final ISysUserService userService;

    @Operation(summary = "分页查询用户列表")
    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/list")
    public R<PageResult<SysUser>> list(SysUser user,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(userService.selectUserPage(user, pageNum, pageSize));
    }

    @Operation(summary = "查询用户详情（含已分配角色）")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/{userId}")
    public R<SysUser> getInfo(@PathVariable Long userId) {
        SysUser user = userService.selectUserById(userId);
        if (user == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        List<Long> roleIds = userService.selectRoleIdsByUserId(userId);
        user.setRoleIds(roleIds.toArray(new Long[0]));
        return R.ok(user);
    }

    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysUser user) {
        return R.toAjax(userService.insertUser(user));
    }

    @Operation(summary = "修改用户")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysUser user) {
        return R.toAjax(userService.updateUser(user));
    }

    @Operation(summary = "删除用户（支持批量，用逗号分隔）")
    @PreAuthorize("hasAuthority('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable Long[] userIds) {
        return R.toAjax(userService.deleteUserByIds(userIds));
    }

    @Operation(summary = "重置密码")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestBody SysUser user) {
        return R.toAjax(userService.resetPassword(user.getUserId(), user.getPassword()));
    }

    @Operation(summary = "启用/停用用户")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysUser user) {
        return R.toAjax(userService.changeStatus(user.getUserId(), user.getStatus()));
    }
}
