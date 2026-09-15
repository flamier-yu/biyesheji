package com.biyesheji.pms.module.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.system.entity.SysOperLog;
import com.biyesheji.pms.module.system.mapper.SysOperLogMapper;
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
 * 操作日志查询
 */
@Tag(name = "09. 操作日志", description = "系统操作日志查询与清理")
@RestController
@RequestMapping("/system/operlog")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogMapper operLogMapper;

    @Operation(summary = "分页查询操作日志")
    @PreAuthorize("hasAuthority('system:operlog:list')")
    @GetMapping("/list")
    public R<PageResult<SysOperLog>> list(SysOperLog operLog,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (operLog != null) {
            if (StrUtil.isNotBlank(operLog.getTitle())) {
                wrapper.like(SysOperLog::getTitle, operLog.getTitle());
            }
            if (StrUtil.isNotBlank(operLog.getOperName())) {
                wrapper.like(SysOperLog::getOperName, operLog.getOperName());
            }
            if (operLog.getBusinessType() != null) {
                wrapper.eq(SysOperLog::getBusinessType, operLog.getBusinessType());
            }
            if (operLog.getStatus() != null) {
                wrapper.eq(SysOperLog::getStatus, operLog.getStatus());
            }
        }
        wrapper.orderByDesc(SysOperLog::getOperId);
        Page<SysOperLog> page = new Page<>(pageNum, pageSize);
        operLogMapper.selectPage(page, wrapper);
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "删除操作日志（支持批量）")
    @PreAuthorize("hasAuthority('system:operlog:remove')")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{operIds}")
    public R<Void> remove(@PathVariable Long[] operIds) {
        return R.toAjax(operLogMapper.deleteBatchIds(Arrays.asList(operIds)) > 0);
    }

    @Operation(summary = "清空操作日志")
    @PreAuthorize("hasAuthority('system:operlog:remove')")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogMapper.cleanOperLog();
        return R.ok();
    }
}
