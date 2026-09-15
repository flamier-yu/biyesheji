package com.biyesheji.pms.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询用户的权限标识集合
     */
    @Select("select distinct m.perms from sys_menu m " +
            "left join sys_role_menu rm on m.menu_id = rm.menu_id " +
            "left join sys_user_role ur on rm.role_id = ur.role_id " +
            "left join sys_role r on r.role_id = ur.role_id " +
            "where ur.user_id = #{userId} and m.status = '0' and r.status = '0' and r.del_flag = '0' " +
            "  and m.perms is not null and m.perms <> ''")
    List<String> selectMenuPermsByUserId(@Param("userId") Long userId);

    /**
     * 查询全部权限标识（超级管理员用）
     */
    @Select("select distinct perms from sys_menu where status = '0' and perms is not null and perms <> ''")
    List<String> selectMenuPermsAll();

    /**
     * 查询用户的菜单树（仅目录与菜单）
     */
    @Select("select distinct m.* from sys_menu m " +
            "left join sys_role_menu rm on m.menu_id = rm.menu_id " +
            "left join sys_user_role ur on rm.role_id = ur.role_id " +
            "left join sys_role r on r.role_id = ur.role_id " +
            "where ur.user_id = #{userId} and m.menu_type in ('M','C') " +
            "  and m.status = '0' and r.status = '0' and r.del_flag = '0' " +
            "order by m.parent_id, m.order_num")
    List<SysMenu> selectMenuTreeByUserId(@Param("userId") Long userId);

    /**
     * 查询全部菜单树（超级管理员用）
     */
    @Select("select * from sys_menu where menu_type in ('M','C') and status = '0' " +
            "order by parent_id, order_num")
    List<SysMenu> selectMenuTreeAll();

    /**
     * 统计菜单下的子菜单数量
     */
    @Select("select count(1) from sys_menu where parent_id = #{menuId}")
    int countChildren(@Param("menuId") Long menuId);

    /**
     * 统计菜单被角色引用数量
     */
    @Select("select count(1) from sys_role_menu where menu_id = #{menuId}")
    int countRoleAssigned(@Param("menuId") Long menuId);
}
