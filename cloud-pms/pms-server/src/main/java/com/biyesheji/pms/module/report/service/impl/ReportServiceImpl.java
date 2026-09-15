package com.biyesheji.pms.module.report.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.framework.websocket.WebSocketServer;
import com.biyesheji.pms.module.project.entity.PmProject;
import com.biyesheji.pms.module.project.mapper.PmProjectMapper;
import com.biyesheji.pms.module.report.domain.WorkLogExportVo;
import com.biyesheji.pms.module.report.entity.PmProjectBurndown;
import com.biyesheji.pms.module.report.mapper.PmProjectBurndownMapper;
import com.biyesheji.pms.module.report.mapper.ReportMapper;
import com.biyesheji.pms.module.report.service.IReportService;
import com.biyesheji.pms.module.system.entity.SysUser;
import com.biyesheji.pms.module.system.mapper.SysUserMapper;
import com.biyesheji.pms.module.task.entity.PmTask;
import com.biyesheji.pms.module.task.mapper.PmTaskMapper;
import com.biyesheji.pms.module.worklog.entity.PmWorkLog;
import com.biyesheji.pms.module.worklog.mapper.PmWorkLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 统计报表服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private final ReportMapper reportMapper;
    private final PmWorkLogMapper workLogMapper;
    private final PmProjectBurndownMapper burndownMapper;
    private final PmProjectMapper projectMapper;
    private final PmTaskMapper taskMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public Map<String, Object> selectOverview() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("projectTotal", reportMapper.countProjects());
        map.put("projectRunning", reportMapper.countRunningProjects());
        map.put("projectDone", reportMapper.countDoneProjects());
        map.put("taskTotal", reportMapper.countTasks());
        map.put("taskDone", reportMapper.countDoneTasks());
        map.put("taskOverdue", reportMapper.countOverdueTasks());
        map.put("userTotal", reportMapper.countUsers());
        Double hours = reportMapper.sumPassedHours();
        map.put("totalHours", hours == null ? 0 : hours);
        // 当前 WebSocket 在线人数
        map.put("onlineCount", WebSocketServer.onlineCount());
        return map;
    }

    @Override
    public List<Map<String, Object>> selectProjectStatusStats() {
        return reportMapper.countProjectByStatus();
    }

    @Override
    public List<Map<String, Object>> selectTaskStatusStats() {
        return reportMapper.countTaskByStatus();
    }

    @Override
    public List<Map<String, Object>> selectTaskPriorityStats() {
        return reportMapper.countTaskByPriority();
    }

    @Override
    public List<Map<String, Object>> selectWorkHoursTrend(Integer days) {
        int span = (days == null || days <= 0) ? 14 : days;
        String startDate = DateUtil.format(DateUtil.offsetDay(new java.util.Date(), -(span - 1)), "yyyy-MM-dd");
        List<Map<String, Object>> raw = workLogMapper.sumHoursByDate(startDate);

        // 补齐没有数据的日期，保证折线图连续
        Map<String, Object> filled = new LinkedHashMap<>();
        for (Map<String, Object> row : raw) {
            filled.put(String.valueOf(row.get("workDate")), row.get("hours"));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < span; i++) {
            String date = DateUtil.format(DateUtil.offsetDay(new java.util.Date(), -(span - 1 - i)), "yyyy-MM-dd");
            Map<String, Object> item = new HashMap<>(2);
            item.put("workDate", date);
            item.put("hours", filled.getOrDefault(date, 0));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectWorkHoursByProject() {
        return workLogMapper.sumHoursByProject();
    }

    @Override
    public List<Map<String, Object>> selectMemberPerformance(Integer limit) {
        return workLogMapper.sumHoursByUser((limit == null || limit <= 0) ? 10 : limit);
    }

    @Override
    public List<Map<String, Object>> selectProjectProgress(Integer limit) {
        return reportMapper.selectProjectProgress((limit == null || limit <= 0) ? 10 : limit);
    }

    @Override
    public List<Map<String, Object>> selectBurndown(Long projectId) {
        if (projectId == null) {
            return new ArrayList<>();
        }
        return reportMapper.selectBurndown(projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateBurndownSnapshot() {
        List<Long> projectIds = reportMapper.selectActiveProjectIds();
        if (CollUtil.isEmpty(projectIds)) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Long projectId : projectIds) {
            Double total = reportMapper.sumEstimateHours(projectId);
            Double remain = reportMapper.sumRemainHours(projectId);
            BigDecimal totalHours = BigDecimal.valueOf(total == null ? 0 : total);
            BigDecimal remainHours = BigDecimal.valueOf(remain == null ? 0 : remain);
            BigDecimal doneHours = totalHours.subtract(remainHours);
            if (doneHours.compareTo(BigDecimal.ZERO) < 0) {
                doneHours = BigDecimal.ZERO;
            }

            // 幂等：同一天重复执行时先清理旧快照
            burndownMapper.delete(new LambdaQueryWrapper<PmProjectBurndown>()
                    .eq(PmProjectBurndown::getProjectId, projectId)
                    .eq(PmProjectBurndown::getSnapshotDate, today));

            PmProjectBurndown snapshot = new PmProjectBurndown();
            snapshot.setProjectId(projectId);
            snapshot.setSnapshotDate(today);
            snapshot.setTotalHours(totalHours);
            snapshot.setRemainingHours(remainHours);
            snapshot.setCompletedHours(doneHours);
            snapshot.setCreateTime(LocalDateTime.now());
            burndownMapper.insert(snapshot);
            count++;
        }
        log.info("燃尽图快照生成完成，共 {} 个项目", count);
        return count;
    }

    @Override
    public void exportWorkLog(HttpServletResponse response, Long projectId) {
        LambdaQueryWrapper<PmWorkLog> wrapper = new LambdaQueryWrapper<PmWorkLog>()
                .eq(PmWorkLog::getStatus, "1");
        if (projectId != null) {
            wrapper.eq(PmWorkLog::getProjectId, projectId);
        }
        wrapper.orderByDesc(PmWorkLog::getWorkDate);
        List<PmWorkLog> logs = workLogMapper.selectList(wrapper);
        List<WorkLogExportVo> rows = convert(logs);

        try {
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("工时报表", "UTF-8").replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), WorkLogExportVo.class)
                    .sheet("工时明细")
                    .doWrite(rows);
        } catch (IOException e) {
            log.error("导出工时报表失败", e);
            throw new ServiceException("导出工时报表失败");
        }
    }

    /**
     * 转换为导出模型，并补齐名称字段
     */
    private List<WorkLogExportVo> convert(List<PmWorkLog> logs) {
        if (CollUtil.isEmpty(logs)) {
            return new ArrayList<>();
        }
        Map<Long, String> projectMap = projectMapper.selectBatchIds(logs.stream()
                        .map(PmWorkLog::getProjectId).filter(Objects::nonNull).distinct()
                        .collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(PmProject::getProjectId, PmProject::getProjectName, (a, b) -> a));

        List<Long> taskIds = logs.stream().map(PmWorkLog::getTaskId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> taskMap = taskIds.isEmpty() ? new HashMap<>()
                : taskMapper.selectBatchIds(taskIds).stream()
                .collect(Collectors.toMap(PmTask::getTaskId, PmTask::getTaskName, (a, b) -> a));

        Map<Long, String> userMap = sysUserMapper.selectBatchIds(logs.stream()
                        .map(PmWorkLog::getUserId).filter(Objects::nonNull).distinct()
                        .collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName, (a, b) -> a));

        List<WorkLogExportVo> rows = new ArrayList<>(logs.size());
        for (PmWorkLog w : logs) {
            WorkLogExportVo vo = new WorkLogExportVo();
            vo.setProjectName(projectMap.get(w.getProjectId()));
            vo.setTaskName(taskMap.get(w.getTaskId()));
            vo.setNickName(userMap.get(w.getUserId()));
            vo.setWorkDate(w.getWorkDate() == null ? "" : w.getWorkDate().toString());
            vo.setHours(w.getHours() == null ? 0d : w.getHours().doubleValue());
            vo.setContent(StrUtil.emptyIfNull(w.getContent()));
            vo.setStatusText("已通过");
            rows.add(vo);
        }
        return rows;
    }
}
