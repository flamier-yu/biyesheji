package com.biyesheji.pms.module.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.project.entity.PmProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 项目 Mapper
 */
@Mapper
public interface PmProjectMapper extends BaseMapper<PmProject> {

    @Select("select count(1) from pm_project where project_code = #{projectCode} and del_flag = '0'")
    int countByCode(@Param("projectCode") String projectCode);

    /**
     * 统计项目下的任务总数（用于自动计算进度）
     */
    @Select("select count(1) from pm_task where project_id = #{projectId} and del_flag = '0'")
    int countTasks(@Param("projectId") Long projectId);

    /**
     * 统计项目下已完成的任务数
     */
    @Select("select count(1) from pm_task where project_id = #{projectId} and del_flag = '0' and status = '2'")
    int countDoneTasks(@Param("projectId") Long projectId);
}
