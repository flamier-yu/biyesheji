package com.biyesheji.pms.module.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.system.entity.SysLoginLog;
import com.biyesheji.pms.module.system.mapper.SysLoginLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 登录日志查询
 */
@Tag(name = "10. 登录日志", description = "系统登录日志查询与清理")
@RestController
@RequestMapping("/system/loginlog")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogMapper loginLogMapper;

    @Operation(summary = "分页查询登录日志")
    @PreAuthorize("hasAuthority('system:loginlog:list')")
    @GetMapping("/list")
    public R<PageResult<SysLoginLog>> list(SysLoginLog loginLog,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (loginLog != null) {
            if (StrUtil.isNotBlank(loginLog.getUsername())) {
                wrapper.like(SysLoginLog::getUsername, loginLog.getUsername());
            }
            if (StrUtil.isNotBlank(loginLog.getIpaddr())) {
                wrapper.like(SysLoginLog::getIpaddr, loginLog.getIpaddr());
            }
            if (StrUtil.isNotBlank(loginLog.getStatus())) {
                wrapper.eq(SysLoginLog::getStatus, loginLog.getStatus());
            }
        }
        wrapper.orderByDesc(SysLoginLog::getInfoId);
        Page<SysLoginLog> page = new Page<>(pageNum, pageSize);
        loginLogMapper.selectPage(page, wrapper);
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "删除登录日志（支持批量）")
    @PreAuthorize("hasAuthority('system:loginlog:remove')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public R<Void> remove(@PathVariable Long[] infoIds) {
        return R.toAjax(loginLogMapper.deleteBatchIds(Arrays.asList(infoIds)) > 0);
    }

    @Operation(summary = "清空登录日志")
    @PreAuthorize("hasAuthority('system:loginlog:remove')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        loginLogMapper.cleanLoginLog();
        return R.ok();
    }
}
