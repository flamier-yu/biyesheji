package com.biyesheji.pms.module.worklog.controller;

import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.worklog.entity.PmWorkLog;
import com.biyesheji.pms.module.worklog.service.IPmWorkLogService;
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
 * 工时管理
 */
@Tag(name = "13. 工时管理", description = "工时填报、审批与查询")
@RestController
@RequestMapping("/worklog")
@RequiredArgsConstructor
@Validated
public class PmWorkLogController {

    private final IPmWorkLogService workLogService;

    @Operation(summary = "分页查询工时列表")
    @PreAuthorize("hasAuthority('project:worklog:list')")
    @GetMapping("/list")
    public R<PageResult<PmWorkLog>> list(PmWorkLog workLog,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(workLogService.selectWorkLogPage(workLog, pageNum, pageSize));
    }

    @Operation(summary = "查询我的工时")
    @GetMapping("/my")
    public R<List<PmWorkLog>> my(@RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate) {
        return R.ok(workLogService.selectMyLogs(startDate, endDate));
    }

    @Operation(summary = "填报工时")
    @Log(title = "工时管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody PmWorkLog workLog) {
        return R.toAjax(workLogService.insertWorkLog(workLog));
    }

    @Operation(summary = "修改工时")
    @Log(title = "工时管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody PmWorkLog workLog) {
        return R.toAjax(workLogService.updateWorkLog(workLog));
    }

    @Operation(summary = "删除工时（支持批量）")
    @Log(title = "工时管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{logIds}")
    public R<Void> remove(@PathVariable Long[] logIds) {
        return R.toAjax(workLogService.deleteWorkLogByIds(logIds));
    }

    @Operation(summary = "审批工时（通过/驳回）")
    @PreAuthorize("hasAuthority('project:worklog:audit')")
    @Log(title = "工时管理", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public R<Void> audit(@RequestBody PmWorkLog workLog) {
        return R.toAjax(workLogService.audit(
                workLog.getLogIds(), workLog.getStatus(), workLog.getAuditRemark()));
    }
}
