package com.biyesheji.pms.module.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.task.entity.PmTaskComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务评论 Mapper
 */
@Mapper
public interface PmTaskCommentMapper extends BaseMapper<PmTaskComment> {

    @Select("select c.*, u.nick_name from pm_task_comment c " +
            "left join sys_user u on u.user_id = c.user_id " +
            "where c.task_id = #{taskId} and c.del_flag = '0' " +
            "order by c.comment_id desc")
    List<PmTaskComment> selectByTaskId(@Param("taskId") Long taskId);

    @Delete("delete from pm_task_comment where task_id = #{taskId}")
    int deleteByTaskId(@Param("taskId") Long taskId);
}
