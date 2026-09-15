package com.biyesheji.pms.module.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统计报表 Mapper
 * <p>
 * 全部为聚合查询，返回 Map/List&lt;Map&gt; 时显式指定驼峰别名。
 */
@Mapper
public interface ReportMapper {

    /* ==================== 总览指标 ==================== */

    @Select("select count(1) from pm_project where del_flag = '0'")
    int countProjects();

    @Select("select count(1) from pm_project where del_flag = '0' and status = '1'")
    int countRunningProjects();

    @Select("select count(1) from pm_project where del_flag = '0' and status = '2'")
    int countDoneProjects();

    @Select("select count(1) from pm_task where del_flag = '0'")
    int countTasks();

    @Select("select count(1) from pm_task where del_flag = '0' and status = '2'")
    int countDoneTasks();

    @Select("select count(1) from pm_task where del_flag = '0' and status in ('0','1') " +
            "and plan_end is not null and plan_end < curdate()")
    int countOverdueTasks();

    @Select("select count(1) from sys_user where del_flag = '0' and status = '0'")
    int countUsers();

    @Select("select ifnull(sum(hours),0) from pm_work_log where del_flag = '0' and status = '1'")
    Double sumPassedHours();

    /* ==================== 分布统计 ==================== */

    @Select("select status as name, count(1) as value from pm_project " +
            "where del_flag = '0' group by status")
    List<Map<String, Object>> countProjectByStatus();

    @Select("select status as name, count(1) as value from pm_task " +
            "where del_flag = '0' group by status")
    List<Map<String, Object>> countTaskByStatus();

    @Select("select priority as name, count(1) as value from pm_task " +
            "where del_flag = '0' group by priority")
    List<Map<String, Object>> countTaskByPriority();

    /* ==================== 项目进度 ==================== */

    @Select("select p.project_id as projectId, p.project_name as projectName, " +
            "p.progress as progress, p.status as status, p.priority as priority, " +
            "count(t.task_id) as taskCount, " +
            "sum(case when t.status = '2' then 1 else 0 end) as doneCount " +
            "from pm_project p " +
            "left join pm_task t on t.project_id = p.project_id and t.del_flag = '0' " +
            "where p.del_flag = '0' " +
            "group by p.project_id, p.project_name, p.progress, p.status, p.priority " +
            "order by p.project_id desc limit #{limit}")
    List<Map<String, Object>> selectProjectProgress(@Param("limit") Integer limit);

    /* ==================== 燃尽图 ==================== */

    @Select("select date_format(snapshot_date, '%Y-%m-%d') as snapshotDate, " +
            "total_hours as totalHours, remaining_hours as remainingHours, " +
            "completed_hours as completedHours " +
            "from pm_project_burndown where project_id = #{projectId} " +
            "order by snapshot_date")
    List<Map<String, Object>> selectBurndown(@Param("projectId") Long projectId);

    /**
     * 生成指定项目的当日快照（总工时 = 预估工时之和，剩余 = 未完成任务的预估工时之和）
     */
    @Select("select ifnull(sum(estimate_hours),0) from pm_task " +
            "where del_flag = '0' and project_id = #{projectId}")
    Double sumEstimateHours(@Param("projectId") Long projectId);

    @Select("select ifnull(sum(estimate_hours),0) from pm_task " +
            "where del_flag = '0' and project_id = #{projectId} and status <> '2'")
    Double sumRemainHours(@Param("projectId") Long projectId);

    @Select("select project_id from pm_project where del_flag = '0' and status <> '4'")
    List<Long> selectActiveProjectIds();
}
