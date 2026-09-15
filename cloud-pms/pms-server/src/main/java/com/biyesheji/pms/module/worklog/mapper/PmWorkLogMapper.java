package com.biyesheji.pms.module.worklog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.worklog.entity.PmWorkLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 工时 Mapper
 * <p>
 * 注意：Map/List&lt;Map&gt; 返回类型不受 map-underscore-to-camel-case 影响，
 * 因此统计 SQL 一律显式指定驼峰别名。
 */
@Mapper
public interface PmWorkLogMapper extends BaseMapper<PmWorkLog> {

    /**
     * 单条工时详情（带项目名、任务名、填报人）
     */
    @Select("select w.*, p.project_name, t.task_name, u.nick_name " +
            "from pm_work_log w " +
            "left join pm_project p on p.project_id = w.project_id " +
            "left join pm_task t on t.task_id = w.task_id " +
            "left join sys_user u on u.user_id = w.user_id " +
            "where w.log_id = #{logId}")
    PmWorkLog selectDetailById(@Param("logId") Long logId);

    /**
     * 按项目汇总工时
     */
    @Select("select p.project_name as projectName, ifnull(sum(w.hours),0) as hours " +
            "from pm_work_log w left join pm_project p on p.project_id = w.project_id " +
            "where w.del_flag = '0' and w.status = '1' " +
            "group by w.project_id, p.project_name order by hours desc")
    List<Map<String, Object>> sumHoursByProject();

    /**
     * 按日期汇总工时（趋势图）
     */
    @Select("select date_format(w.work_date, '%Y-%m-%d') as workDate, ifnull(sum(w.hours),0) as hours " +
            "from pm_work_log w " +
            "where w.del_flag = '0' and w.status = '1' and w.work_date >= #{startDate} " +
            "group by w.work_date order by w.work_date")
    List<Map<String, Object>> sumHoursByDate(@Param("startDate") String startDate);

    /**
     * 按人员汇总工时（绩效排行）
     */
    @Select("select u.nick_name as nickName, ifnull(sum(w.hours),0) as hours " +
            "from pm_work_log w left join sys_user u on u.user_id = w.user_id " +
            "where w.del_flag = '0' and w.status = '1' " +
            "group by w.user_id, u.nick_name order by hours desc limit #{limit}")
    List<Map<String, Object>> sumHoursByUser(@Param("limit") Integer limit);

    /**
     * 某人在某天的已填工时合计（用于校验每日上限）
     */
    @Select("select ifnull(sum(hours),0) from pm_work_log " +
            "where del_flag = '0' and user_id = #{userId} and work_date = #{workDate} " +
            "and log_id <> #{excludeId}")
    BigDecimal sumUserDayHours(@Param("userId") Long userId,
                               @Param("workDate") String workDate,
                               @Param("excludeId") Long excludeId);

    /**
     * 汇总全部已完成工时
     */
    @Select("select ifnull(sum(hours),0) from pm_work_log where del_flag = '0' and status = '1'")
    BigDecimal sumAllPassedHours();
}
