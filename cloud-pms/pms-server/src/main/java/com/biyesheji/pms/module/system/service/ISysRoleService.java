package com.biyesheji.pms.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.module.system.entity.SysRole;

import java.util.List;

/**
 * 角色服务
 */
public interface ISysRoleService extends IService<SysRole> {

    PageResult<SysRole> selectRolePage(SysRole role, Integer pageNum, Integer pageSize);

    /**
     * 查询全部可用角色（供用户分配角色使用）
     */
    List<SysRole> selectRoleAll();

    /**
     * 查询角色已分配的菜单ID
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    boolean insertRole(SysRole role);

    boolean updateRole(SysRole role);

    boolean deleteRoleByIds(Long[] roleIds);

    boolean changeStatus(Long roleId, String status);

    void checkRoleKeyUnique(SysRole role);

    void checkRoleCanDelete(Long roleId);
}
