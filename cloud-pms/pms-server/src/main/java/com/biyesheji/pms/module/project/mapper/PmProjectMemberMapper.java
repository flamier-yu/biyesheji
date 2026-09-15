package com.biyesheji.pms.module.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.project.entity.PmProjectMember;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目成员 Mapper
 */
@Mapper
public interface PmProjectMemberMapper extends BaseMapper<PmProjectMember> {

    /**
     * 查询项目成员（带用户昵称）
     */
    @Select("select m.*, u.nick_name, u.username from pm_project_member m " +
            "left join sys_user u on u.user_id = m.user_id " +
            "where m.project_id = #{projectId} order by m.id")
    List<PmProjectMember> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询用户参与的项目ID
     */
    @Select("select project_id from pm_project_member where user_id = #{userId}")
    List<Long> selectProjectIdsByUserId(@Param("userId") Long userId);

    @Delete("delete from pm_project_member where project_id = #{projectId}")
    int deleteByProjectId(@Param("projectId") Long projectId);

    @Insert("<script>insert into pm_project_member(project_id, user_id, role_in_pj, join_time, create_time) values " +
            "<foreach collection='userIds' item='userId' separator=','>" +
            "(#{projectId}, #{userId}, #{roleInPj}, now(), now())</foreach>" +
            "</script>")
    int batchInsert(@Param("projectId") Long projectId,
                    @Param("userIds") List<Long> userIds,
                    @Param("roleInPj") String roleInPj);
}
