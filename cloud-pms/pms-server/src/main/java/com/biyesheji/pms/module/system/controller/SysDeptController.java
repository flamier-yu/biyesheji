package com.biyesheji.pms.module.system.controller;

import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.system.entity.SysDept;
import com.biyesheji.pms.module.system.service.ISysDeptService;
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
 * 部门管理
 */
@Tag(name = "02. 部门管理", description = "部门增删改查与树形结构")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
@Validated
public class SysDeptController {

    private final ISysDeptService deptService;

    @Operation(summary = "查询部门列表（平铺）")
    @PreAuthorize("hasAuthority('system:dept:list')")
    @GetMapping("/list")
    public R<List<SysDept>> list(SysDept dept) {
        return R.ok(deptService.selectDeptList(dept));
    }

    @Operation(summary = "查询部门树（供下拉选择使用）")
    @GetMapping("/tree")
    public R<List<SysDept>> tree() {
        return R.ok(deptService.buildDeptTree(deptService.selectDeptList(new SysDept())));
    }

    @Operation(summary = "查询部门详情")
    @PreAuthorize("hasAuthority('system:dept:query')")
    @GetMapping("/{deptId}")
    public R<SysDept> getInfo(@PathVariable Long deptId) {
        return R.ok(deptService.getById(deptId));
    }

    @Operation(summary = "新增部门")
    @PreAuthorize("hasAuthority('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDept dept) {
        return R.toAjax(deptService.insertDept(dept));
    }

    @Operation(summary = "修改部门")
    @PreAuthorize("hasAuthority('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysDept dept) {
        return R.toAjax(deptService.updateDept(dept));
    }

    @Operation(summary = "删除部门")
    @PreAuthorize("hasAuthority('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable Long deptId) {
        deptService.checkDeptCanDelete(deptId);
        return R.toAjax(deptService.removeById(deptId));
    }
}
