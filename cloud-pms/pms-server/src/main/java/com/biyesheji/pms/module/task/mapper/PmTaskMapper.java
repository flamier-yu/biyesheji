package com.biyesheji.pms.module.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.task.entity.PmTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务 Mapper
 */
@Mapper
public interface PmTaskMapper extends BaseMapper<PmTask> {

    /**
     * 查询项目下任务（带负责人与项目名）
     */
    @Select("select t.*, u.nick_name as assignee_name, p.project_name " +
            "from pm_task t " +
            "left join sys_user u on u.user_id = t.assignee_id " +
            "left join pm_project p on p.project_id = t.project_id " +
            "where t.del_flag = '0' and t.project_id = #{projectId} " +
            "order by t.order_num, t.task_id")
    List<PmTask> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询我负责的任务
     */
    @Select("select t.*, u.nick_name as assignee_name, p.project_name " +
            "from pm_task t " +
            "left join sys_user u on u.user_id = t.assignee_id " +
            "left join pm_project p on p.project_id = t.project_id " +
            "where t.del_flag = '0' and t.assignee_id = #{userId} " +
            "order by t.plan_end, t.task_id")
    List<PmTask> selectByAssigneeId(@Param("userId") Long userId);

    @Select("select count(1) from pm_task where project_id = #{projectId} and del_flag = '0'")
    int countAll(@Param("projectId") Long projectId);

    @Select("select count(1) from pm_task where project_id = #{projectId} and del_flag = '0' and status = '2'")
    int countDone(@Param("projectId") Long projectId);

    @Select("select count(1) from pm_task where parent_id = #{taskId} and del_flag = '0'")
    int countChildren(@Param("taskId") Long taskId);

    /**
     * 已逾期任务（未完成且计划结束日期早于今天）
     */
    @Select("select * from pm_task where del_flag = '0' and status in ('0','1') " +
            "and plan_end is not null and plan_end < curdate()")
    List<PmTask> selectOverdueTasks();

    /**
     * 即将到期任务（未完成且计划结束日期在 N 天内）
     */
    @Select("select * from pm_task where del_flag = '0' and status in ('0','1') " +
            "and plan_end is not null and plan_end >= curdate() " +
            "and plan_end <= date_add(curdate(), interval #{days} day)")
    List<PmTask> selectDueSoonTasks(@Param("days") Integer days);
}
