package com.biyesheji.pms.module.system.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色-菜单关联 Mapper
 */
@Mapper
public interface SysRoleMenuMapper {

    @Insert("<script>insert into sys_role_menu(role_id, menu_id) values " +
            "<foreach collection='menuIds' item='menuId' separator=','>(#{roleId}, #{menuId})</foreach>" +
            "</script>")
    int batchInsert(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    @Delete("delete from sys_role_menu where role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Delete("<script>delete from sys_role_menu where role_id in " +
            "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>#{roleId}</foreach>" +
            "</script>")
    int deleteByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Select("select menu_id from sys_role_menu where role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    @Select("select count(1) from sys_role_menu where menu_id = #{menuId}")
    int countByMenuId(@Param("menuId") Long menuId);
}
