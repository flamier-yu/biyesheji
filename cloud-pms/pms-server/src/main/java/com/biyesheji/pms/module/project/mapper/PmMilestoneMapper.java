package com.biyesheji.pms.module.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.project.entity.PmMilestone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 里程碑 Mapper
 */
@Mapper
public interface PmMilestoneMapper extends BaseMapper<PmMilestone> {

    @Select("select * from pm_milestone where project_id = #{projectId} and del_flag = '0' " +
            "order by order_num, milestone_id")
    List<PmMilestone> selectByProjectId(@Param("projectId") Long projectId);
}
