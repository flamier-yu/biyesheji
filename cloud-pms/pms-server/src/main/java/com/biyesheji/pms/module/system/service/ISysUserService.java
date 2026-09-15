package com.biyesheji.pms.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.module.system.entity.SysUser;

import java.util.List;
import java.util.Set;

/**
 * 用户服务
 */
public interface ISysUserService extends IService<SysUser> {

    /* ========== 认证相关 ========== */

    SysUser selectUserByUsername(String username);

    SysUser selectUserById(Long userId);

    Set<String> selectRoleKeysByUserId(Long userId);

    List<Long> selectRoleIdsByUserId(Long userId);

    void updateLoginInfo(Long userId, String loginIp);

    /* ========== 管理相关 ========== */

    /**
     * 分页查询用户列表
     */
    PageResult<SysUser> selectUserPage(SysUser user, Integer pageNum, Integer pageSize);

    /**
     * 新增用户（含角色绑定）
     */
    boolean insertUser(SysUser user);

    /**
     * 修改用户（含角色重绑）
     */
    boolean updateUser(SysUser user);

    /**
     * 批量删除用户
     */
    boolean deleteUserByIds(Long[] userIds);

    /**
     * 重置密码
     */
    boolean resetPassword(Long userId, String rawPassword);

    /**
     * 修改用户状态
     */
    boolean changeStatus(Long userId, String status);

    /**
     * 校验账号是否唯一
     */
    void checkUsernameUnique(SysUser user);

    /**
     * 校验手机号是否唯一
     */
    void checkPhoneUnique(SysUser user);

    /**
     * 校验邮箱是否唯一
     */
    void checkEmailUnique(SysUser user);
}
