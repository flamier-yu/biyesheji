package com.biyesheji.pms.framework.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.biyesheji.pms.module.message.service.IPmMessageService;
import com.biyesheji.pms.module.project.entity.PmProject;
import com.biyesheji.pms.module.project.mapper.PmProjectMapper;
import com.biyesheji.pms.module.report.service.IReportService;
import com.biyesheji.pms.module.task.entity.PmTask;
import com.biyesheji.pms.module.task.mapper.PmTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 定时任务
 * <p>
 * 通过 cron 表达式驱动，与业务代码解耦；无需额外引入调度中间件。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    /** 逾期预警提前天数 */
    private static final int DUE_SOON_DAYS = 1;

    private final IReportService reportService;
    private final PmTaskMapper taskMapper;
    private final PmProjectMapper projectMapper;
    private final IPmMessageService messageService;

    /**
     * 每天 01:00 为进行中的项目生成燃尽图快照
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateBurndownSnapshot() {
        try {
            int count = reportService.generateBurndownSnapshot();
            log.info("[定时任务] 燃尽图快照生成完成，共 {} 个项目", count);
        } catch (Exception e) {
            log.error("[定时任务] 燃尽图快照生成失败", e);
        }
    }

    /**
     * 每天 09:00 扫描逾期任务，向负责人与项目经理发送预警
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void scanOverdueTasks() {
        try {
            List<PmTask> overdue = taskMapper.selectOverdueTasks();
            if (CollUtil.isEmpty(overdue)) {
                log.info("[定时任务] 无逾期任务");
                return;
            }
            int sent = 0;
            for (PmTask task : overdue) {
                PmProject project = projectMapper.selectById(task.getProjectId());
                String projectName = project == null ? "未知项目" : project.getProjectName();
                long days = task.getPlanEnd() == null ? 0
                        : DateUtil.betweenDay(java.sql.Date.valueOf(task.getPlanEnd()),
                        new java.util.Date(), true);

                List<Long> receivers = new ArrayList<>();
                if (task.getAssigneeId() != null) {
                    receivers.add(task.getAssigneeId());
                }
                if (project != null && project.getManagerId() != null) {
                    receivers.add(project.getManagerId());
                }
                receivers = receivers.stream().filter(Objects::nonNull).distinct()
                        .collect(Collectors.toList());

                messageService.sendBatch(receivers,
                        "任务逾期预警",
                        String.format("项目「%s」下的任务「%s」已逾期 %d 天（计划结束：%s），请尽快处理。",
                                projectName, task.getTaskName(), days, task.getPlanEnd()),
                        "4", "task", task.getTaskId());
                sent += receivers.size();
            }
            log.info("[定时任务] 逾期任务预警完成，任务 {} 个，通知 {} 人次", overdue.size(), sent);
        } catch (Exception e) {
            log.error("[定时任务] 逾期任务扫描失败", e);
        }
    }

    /**
     * 每天 09:05 扫描即将到期任务，提前一天提醒
     */
    @Scheduled(cron = "0 5 9 * * ?")
    public void scanDueSoonTasks() {
        try {
            List<PmTask> dueSoon = taskMapper.selectDueSoonTasks(DUE_SOON_DAYS);
            if (CollUtil.isEmpty(dueSoon)) {
                return;
            }
            for (PmTask task : dueSoon) {
                if (task.getAssigneeId() == null) {
                    continue;
                }
                messageService.send(task.getAssigneeId(),
                        "任务即将到期",
                        String.format("任务「%s」将于 %s 到期，请及时跟进。",
                                task.getTaskName(), task.getPlanEnd()),
                        "4", "task", task.getTaskId());
            }
            log.info("[定时任务] 即将到期提醒完成，共 {} 个任务", dueSoon.size());
        } catch (Exception e) {
            log.error("[定时任务] 即将到期提醒失败", e);
        }
    }

    /**
     * 每天 00:30 清理过期的验证码等临时缓存（示例：演示 Redis 定时维护）
     */
    @Scheduled(cron = "0 30 0 * * ?")
    public void dailyMaintenance() {
        log.info("[定时任务] 每日维护任务执行，当前日期 {}", LocalDate.now());
    }
}
