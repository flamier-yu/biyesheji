package com.biyesheji.pms.module.system.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户-角色关联 Mapper（无独立实体，直接用注解 SQL）
 */
@Mapper
public interface SysUserRoleMapper {

    /**
     * 批量插入用户角色关联
     */
    @Insert("<script>insert into sys_user_role(user_id, role_id) values " +
            "<foreach collection='roleIds' item='roleId' separator=','>(#{userId}, #{roleId})</foreach>" +
            "</script>")
    int batchInsert(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 删除用户的全部角色关联
     */
    @Delete("delete from sys_user_role where user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 批量删除多个用户的角色关联
     */
    @Delete("<script>delete from sys_user_role where user_id in " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>#{userId}</foreach>" +
            "</script>")
    int deleteByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 查询用户拥有的角色ID
     */
    @Select("select role_id from sys_user_role where user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 统计角色被分配的用户数
     */
    @Select("select count(1) from sys_user_role where role_id = #{roleId}")
    int countByRoleId(@Param("roleId") Long roleId);
}
