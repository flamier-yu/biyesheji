package com.biyesheji.pms.module.report.service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 统计报表服务
 */
public interface IReportService {

    /**
     * 工作台总览指标
     */
    Map<String, Object> selectOverview();

    /**
     * 项目状态分布
     */
    List<Map<String, Object>> selectProjectStatusStats();

    /**
     * 任务状态分布
     */
    List<Map<String, Object>> selectTaskStatusStats();

    /**
     * 任务优先级分布
     */
    List<Map<String, Object>> selectTaskPriorityStats();

    /**
     * 工时趋势（最近 N 天）
     */
    List<Map<String, Object>> selectWorkHoursTrend(Integer days);

    /**
     * 各项目工时占比
     */
    List<Map<String, Object>> selectWorkHoursByProject();

    /**
     * 成员绩效排行
     */
    List<Map<String, Object>> selectMemberPerformance(Integer limit);

    /**
     * 项目进度一览
     */
    List<Map<String, Object>> selectProjectProgress(Integer limit);

    /**
     * 项目燃尽图数据
     */
    List<Map<String, Object>> selectBurndown(Long projectId);

    /**
     * 为所有进行中的项目生成当日燃尽图快照（定时任务调用）
     *
     * @return 生成快照的项目数
     */
    int generateBurndownSnapshot();

    /**
     * 导出工时报表（Excel）
     */
    void exportWorkLog(HttpServletResponse response, Long projectId);
}
