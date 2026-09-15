package com.biyesheji.pms.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biyesheji.pms.module.system.domain.vo.RouterVo;
import com.biyesheji.pms.module.system.entity.SysMenu;

import java.util.List;
import java.util.Set;

/**
 * 菜单服务
 */
public interface ISysMenuService extends IService<SysMenu> {

    /* ========== 认证/路由 ========== */

    /**
     * 查询用户拥有的权限标识集合
     */
    Set<String> selectMenuPermsByUserId(Long userId);

    /**
     * 查询用户可见的菜单树
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 扁平列表构建为树
     */
    List<SysMenu> buildMenuTree(List<SysMenu> menus);

    /**
     * 菜单树转换为前端动态路由
     */
    List<RouterVo> buildMenus(List<SysMenu> menus);

    /* ========== 管理 ========== */

    /**
     * 条件查询菜单列表
     */
    List<SysMenu> selectMenuList(SysMenu menu);

    /**
     * 查询全部菜单树（含按钮，供角色授权使用）
     */
    List<SysMenu> selectAllMenuTree();

    boolean insertMenu(SysMenu menu);

    boolean updateMenu(SysMenu menu);

    boolean deleteMenuById(Long menuId);
}
