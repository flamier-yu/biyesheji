package com.biyesheji.pms.module.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.module.system.entity.SysRole;
import com.biyesheji.pms.module.system.mapper.SysRoleMapper;
import com.biyesheji.pms.module.system.mapper.SysRoleMenuMapper;
import com.biyesheji.pms.module.system.mapper.SysUserRoleMapper;
import com.biyesheji.pms.module.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public PageResult<SysRole> selectRolePage(SysRole role, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            if (StrUtil.isNotBlank(role.getRoleName())) {
                wrapper.like(SysRole::getRoleName, role.getRoleName());
            }
            if (StrUtil.isNotBlank(role.getRoleKey())) {
                wrapper.like(SysRole::getRoleKey, role.getRoleKey());
            }
            if (StrUtil.isNotBlank(role.getStatus())) {
                wrapper.eq(SysRole::getStatus, role.getStatus());
            }
        }
        wrapper.orderByAsc(SysRole::getRoleSort);
        Page<SysRole> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<SysRole> selectRoleAll() {
        return lambdaQuery()
                .eq(SysRole::getStatus, Constants.STATUS_NORMAL)
                .orderByAsc(SysRole::getRoleSort)
                .list();
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        List<Long> menuIds = sysRoleMenuMapper.selectMenuIdsByRoleId(roleId);
        return menuIds == null ? new ArrayList<>() : menuIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRole(SysRole role) {
        checkRoleKeyUnique(role);
        boolean saved = save(role);
        bindMenus(role.getRoleId(), role.getMenuIds());
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        Long roleId = role.getRoleId();
        if (roleId == null) {
            throw new ServiceException("角色ID不能为空");
        }
        if (Constants.ADMIN_ROLE_ID.equals(roleId)) {
            throw new ServiceException(ResultCode.ADMIN_ROLE_CANNOT_OPERATE);
        }
        checkRoleKeyUnique(role);

        boolean updated = updateById(role);
        sysRoleMenuMapper.deleteByRoleId(roleId);
        bindMenus(roleId, role.getMenuIds());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleByIds(Long[] roleIds) {
        if (roleIds == null || roleIds.length == 0) {
            return false;
        }
        for (Long roleId : roleIds) {
            if (Constants.ADMIN_ROLE_ID.equals(roleId)) {
                throw new ServiceException(ResultCode.ADMIN_ROLE_CANNOT_OPERATE);
            }
            checkRoleCanDelete(roleId);
        }
        List<Long> ids = Arrays.asList(roleIds);
        sysRoleMenuMapper.deleteByRoleIds(ids);
        return removeByIds(ids);
    }

    @Override
    public boolean changeStatus(Long roleId, String status) {
        if (Constants.ADMIN_ROLE_ID.equals(roleId)) {
            throw new ServiceException(ResultCode.ADMIN_ROLE_CANNOT_OPERATE);
        }
        SysRole update = new SysRole();
        update.setRoleId(roleId);
        update.setStatus(status);
        return updateById(update);
    }

    @Override
    public void checkRoleKeyUnique(SysRole role) {
        Long roleId = role.getRoleId() == null ? -1L : role.getRoleId();
        SysRole exist = lambdaQuery().eq(SysRole::getRoleKey, role.getRoleKey()).one();
        if (exist != null && !exist.getRoleId().equals(roleId)) {
            throw new ServiceException(ResultCode.ROLE_KEY_EXIST);
        }
    }

    @Override
    public void checkRoleCanDelete(Long roleId) {
        if (sysUserRoleMapper.countByRoleId(roleId) > 0) {
            throw new ServiceException(ResultCode.ROLE_ASSIGNED_CANNOT_DELETE);
        }
    }

    private void bindMenus(Long roleId, Long[] menuIds) {
        if (roleId == null || menuIds == null || menuIds.length == 0) {
            return;
        }
        List<Long> ids = Arrays.stream(menuIds).filter(Objects::nonNull).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            sysRoleMenuMapper.batchInsert(roleId, ids);
        }
    }
}
