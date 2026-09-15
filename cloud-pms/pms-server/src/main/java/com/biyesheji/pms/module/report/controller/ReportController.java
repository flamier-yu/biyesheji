package com.biyesheji.pms.module.report.controller;

import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.report.service.IReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 统计报表
 */
@Tag(name = "16. 统计报表", description = "工作台总览、分布统计、趋势分析、绩效与燃尽图")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    @Operation(summary = "工作台总览指标")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(reportService.selectOverview());
    }

    @Operation(summary = "项目状态分布")
    @GetMapping("/project/status")
    public R<List<Map<String, Object>>> projectStatus() {
        return R.ok(reportService.selectProjectStatusStats());
    }

    @Operation(summary = "任务状态分布")
    @GetMapping("/task/status")
    public R<List<Map<String, Object>>> taskStatus() {
        return R.ok(reportService.selectTaskStatusStats());
    }

    @Operation(summary = "任务优先级分布")
    @GetMapping("/task/priority")
    public R<List<Map<String, Object>>> taskPriority() {
        return R.ok(reportService.selectTaskPriorityStats());
    }

    @Operation(summary = "工时趋势（最近 N 天）")
    @GetMapping("/worklog/trend")
    public R<List<Map<String, Object>>> worklogTrend(@RequestParam(defaultValue = "14") Integer days) {
        return R.ok(reportService.selectWorkHoursTrend(days));
    }

    @Operation(summary = "各项目工时占比")
    @GetMapping("/worklog/project")
    public R<List<Map<String, Object>>> worklogByProject() {
        return R.ok(reportService.selectWorkHoursByProject());
    }

    @Operation(summary = "成员绩效排行")
    @GetMapping("/worklog/performance")
    public R<List<Map<String, Object>>> performance(@RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(reportService.selectMemberPerformance(limit));
    }

    @Operation(summary = "项目进度一览")
    @GetMapping("/project/progress")
    public R<List<Map<String, Object>>> projectProgress(@RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(reportService.selectProjectProgress(limit));
    }

    @Operation(summary = "项目燃尽图数据")
    @GetMapping("/burndown/{projectId}")
    public R<List<Map<String, Object>>> burndown(@PathVariable Long projectId) {
        return R.ok(reportService.selectBurndown(projectId));
    }

    @Operation(summary = "导出行工时报表明细")
    @PreAuthorize("hasAuthority('report:worklog:export')")
    @Log(title = "统计报表", businessType = BusinessType.EXPORT, saveResponseData = false)
    @GetMapping("/worklog/export")
    public void exportWorkLog(@RequestParam(required = false) Long projectId,
                              HttpServletResponse response) {
        reportService.exportWorkLog(response, projectId);
    }
}
