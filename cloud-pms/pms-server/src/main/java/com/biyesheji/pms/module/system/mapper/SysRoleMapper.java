package com.biyesheji.pms.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户拥有的角色标识
     */
    @Select("select distinct r.role_key from sys_role r " +
            "inner join sys_user_role ur on ur.role_id = r.role_id " +
            "where ur.user_id = #{userId} and r.status = '0' and r.del_flag = '0'")
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    /**
     * 查询用户拥有的角色
     */
    @Select("select r.* from sys_role r " +
            "inner join sys_user_role ur on ur.role_id = r.role_id " +
            "where ur.user_id = #{userId} and r.del_flag = '0'")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 统计角色已分配的用户数
     */
    @Select("select count(1) from sys_user_role where role_id = #{roleId}")
    int countUserByRoleId(@Param("roleId") Long roleId);
}
