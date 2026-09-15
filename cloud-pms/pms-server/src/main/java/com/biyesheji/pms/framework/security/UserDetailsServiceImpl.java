package com.biyesheji.pms.framework.security;

import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.module.system.entity.SysUser;
import com.biyesheji.pms.module.system.service.ISysMenuService;
import com.biyesheji.pms.module.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户认证信息加载
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ISysUserService sysUserService;
    private final ISysMenuService sysMenuService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.selectUserByUsername(username);
        log.debug("[AUTH] loadUser username={}, found={}", username, user != null);
        if (user == null) {
            throw new UsernameNotFoundException("登录账号不存在");
        }
        if (Constants.STATUS_DISABLE.equals(user.getStatus())) {
            throw new DisabledException("账号已被停用");
        }
        return buildLoginUser(user);
    }

    /**
     * 把 SysUser 组装为登录身份对象
     */
    public LoginUser buildLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser(
                user.getUserId(),
                user.getDeptId(),
                user.getUsername(),
                user.getNickName(),
                user.getPassword());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setRoles(sysUserService.selectRoleKeysByUserId(user.getUserId()));
        loginUser.setPermissions(sysMenuService.selectMenuPermsByUserId(user.getUserId()));
        return loginUser;
    }
}
