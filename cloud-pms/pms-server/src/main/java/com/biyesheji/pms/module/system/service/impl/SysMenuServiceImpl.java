package com.biyesheji.pms.module.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.module.system.domain.vo.MetaVo;
import com.biyesheji.pms.module.system.domain.vo.RouterVo;
import com.biyesheji.pms.module.system.entity.SysMenu;
import com.biyesheji.pms.module.system.mapper.SysMenuMapper;
import com.biyesheji.pms.module.system.service.ISysMenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    /** 菜单树最大层级，防止脏数据造成无限递归 */
    private static final int MAX_TREE_DEPTH = 20;

    /* ==================== 认证/路由 ==================== */

    @Override
    public Set<String> selectMenuPermsByUserId(Long userId) {
        List<String> perms = Constants.ADMIN_USER_ID.equals(userId)
                ? baseMapper.selectMenuPermsAll()
                : baseMapper.selectMenuPermsByUserId(userId);
        if (perms == null || perms.isEmpty()) {
            return new HashSet<>();
        }
        // 一个 perms 字段可能存逗号分隔的多权限
        Set<String> result = new HashSet<>();
        for (String perm : perms) {
            if (StrUtil.isBlank(perm)) {
                continue;
            }
            for (String p : perm.trim().split(",")) {
                if (StrUtil.isNotBlank(p)) {
                    result.add(p.trim());
                }
            }
        }
        return result;
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> menus = Constants.ADMIN_USER_ID.equals(userId)
                ? baseMapper.selectMenuTreeAll()
                : baseMapper.selectMenuTreeByUserId(userId);
        return buildMenuTree(menus);
    }

    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (Constants.ROOT_PARENT_ID.equals(menu.getParentId())) {
                fillChildren(menu, menus, 1);
                tree.add(menu);
            }
        }
        sortByOrder(tree);
        return tree;
    }

    /**
     * 递归填充子节点
     */
    private void fillChildren(SysMenu parent, List<SysMenu> all, int depth) {
        if (depth > MAX_TREE_DEPTH) {
            return;
        }
        List<SysMenu> children = all.stream()
                .filter(m -> parent.getMenuId().equals(m.getParentId()))
                .sorted(Comparator.comparing(SysMenu::getOrderNum,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        for (SysMenu child : children) {
            fillChildren(child, all, depth + 1);
        }
        parent.setChildren(children);
    }

    private void sortByOrder(List<SysMenu> menus) {
        menus.sort(Comparator.comparing(SysMenu::getOrderNum,
                Comparator.nullsLast(Comparator.naturalOrder())));
    }

    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<RouterVo> routers = new ArrayList<>();
        if (menus == null || menus.isEmpty()) {
            return routers;
        }
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(Constants.STATUS_DISABLE.equals(menu.getVisible()));
            router.setName(toRouteName(menu.getPath()));
            router.setPath(menu.getPath());
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(),
                    Constants.STATUS_DISABLE.equals(menu.getIsCache())));

            List<SysMenu> children = menu.getChildren();
            if (Constants.MENU_TYPE_DIR.equals(menu.getMenuType())) {
                // 目录：使用前端布局组件承载
                router.setComponent("Layout");
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(children));
            } else {
                // 菜单：指向具体页面组件
                router.setComponent(menu.getComponent());
                router.setAlwaysShow(false);
                if (children != null && !children.isEmpty()) {
                    router.setChildren(buildMenus(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 路由地址转路由名称，例如 /system/user -> SystemUser
     */
    private String toRouteName(String path) {
        if (StrUtil.isBlank(path)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : path.split("/")) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    /* ==================== 管理 ==================== */

    @Override
    public List<SysMenu> selectMenuList(SysMenu menu) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        if (menu != null) {
            if (StrUtil.isNotBlank(menu.getMenuName())) {
                wrapper.like(SysMenu::getMenuName, menu.getMenuName());
            }
            if (StrUtil.isNotBlank(menu.getStatus())) {
                wrapper.eq(SysMenu::getStatus, menu.getStatus());
            }
            if (StrUtil.isNotBlank(menu.getMenuType())) {
                wrapper.eq(SysMenu::getMenuType, menu.getMenuType());
            }
        }
        wrapper.orderByAsc(SysMenu::getParentId).orderByAsc(SysMenu::getOrderNum);
        return list(wrapper);
    }

    @Override
    public List<SysMenu> selectAllMenuTree() {
        return buildMenuTree(selectMenuList(new SysMenu()));
    }

    @Override
    public boolean insertMenu(SysMenu menu) {
        return save(menu);
    }

    @Override
    public boolean updateMenu(SysMenu menu) {
        Long menuId = menu.getMenuId();
        if (menuId == null) {
            throw new ServiceException("菜单ID不能为空");
        }
        if (menuId.equals(menu.getParentId())) {
            throw new ServiceException("上级菜单不能是自己");
        }
        return updateById(menu);
    }

    @Override
    public boolean deleteMenuById(Long menuId) {
        if (baseMapper.countChildren(menuId) > 0) {
            throw new ServiceException(ResultCode.MENU_HAS_CHILDREN);
        }
        if (baseMapper.countRoleAssigned(menuId) > 0) {
            throw new ServiceException(ResultCode.MENU_ASSIGNED_CANNOT_DELETE);
        }
        return removeById(menuId);
    }
}
