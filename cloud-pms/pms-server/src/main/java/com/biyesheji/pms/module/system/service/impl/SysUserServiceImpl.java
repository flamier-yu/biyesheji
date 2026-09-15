package com.biyesheji.pms.module.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.module.system.entity.SysDept;
import com.biyesheji.pms.module.system.entity.SysUser;
import com.biyesheji.pms.module.system.mapper.SysDeptMapper;
import com.biyesheji.pms.module.system.mapper.SysRoleMapper;
import com.biyesheji.pms.module.system.mapper.SysUserMapper;
import com.biyesheji.pms.module.system.mapper.SysUserRoleMapper;
import com.biyesheji.pms.module.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysDeptMapper sysDeptMapper;
    private final PasswordEncoder passwordEncoder;

    /* ==================== 认证相关 ==================== */

    @Override
    public SysUser selectUserByUsername(String username) {
        return lambdaQuery().eq(SysUser::getUsername, username).one();
    }

    @Override
    public SysUser selectUserById(Long userId) {
        return getById(userId);
    }

    @Override
    public Set<String> selectRoleKeysByUserId(Long userId) {
        List<String> keys = sysRoleMapper.selectRoleKeysByUserId(userId);
        return keys == null ? new HashSet<>() : new HashSet<>(keys);
    }

    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        List<Long> ids = sysUserRoleMapper.selectRoleIdsByUserId(userId);
        return ids == null ? new ArrayList<>() : ids;
    }

    @Override
    public void updateLoginInfo(Long userId, String loginIp) {
        baseMapper.updateLoginInfo(userId, loginIp);
    }

    /* ==================== 管理相关 ==================== */

    @Override
    public PageResult<SysUser> selectUserPage(SysUser user, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (user != null) {
            if (StrUtil.isNotBlank(user.getUsername())) {
                wrapper.like(SysUser::getUsername, user.getUsername());
            }
            if (StrUtil.isNotBlank(user.getNickName())) {
                wrapper.like(SysUser::getNickName, user.getNickName());
            }
            if (StrUtil.isNotBlank(user.getPhonenumber())) {
                wrapper.like(SysUser::getPhonenumber, user.getPhonenumber());
            }
            if (StrUtil.isNotBlank(user.getStatus())) {
                wrapper.eq(SysUser::getStatus, user.getStatus());
            }
            if (user.getDeptId() != null) {
                wrapper.eq(SysUser::getDeptId, user.getDeptId());
            }
        }
        wrapper.orderByAsc(SysUser::getUserId);

        Page<SysUser> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillDeptName(page.getRecords());
        return PageResult.of(page);
    }

    /**
     * 批量填充部门名称，避免 N+1 查询
     */
    private void fillDeptName(List<SysUser> users) {
        if (CollUtil.isEmpty(users)) {
            return;
        }
        List<Long> deptIds = users.stream()
                .map(SysUser::getDeptId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (deptIds.isEmpty()) {
            return;
        }
        Map<Long, String> deptMap = sysDeptMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName, (a, b) -> a));
        users.forEach(u -> u.setDeptName(deptMap.get(u.getDeptId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertUser(SysUser user) {
        checkUsernameUnique(user);
        checkPhoneUnique(user);
        checkEmailUnique(user);

        String rawPassword = StrUtil.isBlank(user.getPassword())
                ? Constants.DEFAULT_PASSWORD : user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));

        boolean saved = save(user);
        bindRoles(user.getUserId(), user.getRoleIds());
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        Long userId = user.getUserId();
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }
        checkUsernameUnique(user);
        checkPhoneUnique(user);
        checkEmailUnique(user);
        if (Constants.ADMIN_USER_ID.equals(userId)) {
            // 超级管理员的账号与状态不允许被篡改
            SysUser old = getById(userId);
            user.setUsername(old.getUsername());
            user.setStatus(old.getStatus());
        }
        // 密码不允许通过该接口修改
        user.setPassword(null);

        boolean updated = updateById(user);
        // 重建角色关联
        sysUserRoleMapper.deleteByUserId(userId);
        bindRoles(userId, user.getRoleIds());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserByIds(Long[] userIds) {
        if (userIds == null || userIds.length == 0) {
            return false;
        }
        for (Long userId : userIds) {
            if (Constants.ADMIN_USER_ID.equals(userId)) {
                throw new ServiceException(ResultCode.ADMIN_CANNOT_OPERATE);
            }
        }
        List<Long> ids = Arrays.asList(userIds);
        sysUserRoleMapper.deleteByUserIds(ids);
        return removeByIds(ids);
    }

    @Override
    public boolean resetPassword(Long userId, String rawPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        String raw = StrUtil.isBlank(rawPassword) ? Constants.DEFAULT_PASSWORD : rawPassword;
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setPassword(passwordEncoder.encode(raw));
        return updateById(update);
    }

    @Override
    public boolean changeStatus(Long userId, String status) {
        if (Constants.ADMIN_USER_ID.equals(userId)) {
            throw new ServiceException(ResultCode.ADMIN_CANNOT_OPERATE);
        }
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setStatus(status);
        return updateById(update);
    }

    @Override
    public void checkUsernameUnique(SysUser user) {
        Long userId = user.getUserId() == null ? -1L : user.getUserId();
        SysUser exist = lambdaQuery().eq(SysUser::getUsername, user.getUsername()).one();
        if (exist != null && !exist.getUserId().equals(userId)) {
            throw new ServiceException(ResultCode.USERNAME_EXIST);
        }
    }

    @Override
    public void checkPhoneUnique(SysUser user) {
        if (StrUtil.isBlank(user.getPhonenumber())) {
            return;
        }
        Long userId = user.getUserId() == null ? -1L : user.getUserId();
        SysUser exist = lambdaQuery().eq(SysUser::getPhonenumber, user.getPhonenumber()).one();
        if (exist != null && !exist.getUserId().equals(userId)) {
            throw new ServiceException(ResultCode.PHONE_EXIST);
        }
    }

    @Override
    public void checkEmailUnique(SysUser user) {
        if (StrUtil.isBlank(user.getEmail())) {
            return;
        }
        Long userId = user.getUserId() == null ? -1L : user.getUserId();
        SysUser exist = lambdaQuery().eq(SysUser::getEmail, user.getEmail()).one();
        if (exist != null && !exist.getUserId().equals(userId)) {
            throw new ServiceException(ResultCode.EMAIL_EXIST);
        }
    }

    private void bindRoles(Long userId, Long[] roleIds) {
        if (userId == null || roleIds == null || roleIds.length == 0) {
            return;
        }
        List<Long> ids = Arrays.stream(roleIds).filter(Objects::nonNull).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            sysUserRoleMapper.batchInsert(userId, ids);
        }
    }
}
