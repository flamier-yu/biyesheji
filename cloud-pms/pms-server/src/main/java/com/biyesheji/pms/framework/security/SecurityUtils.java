package com.biyesheji.pms.framework.security;

import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

/**
 * 安全上下文工具类
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户，未登录返回 null（用于日志切面等非强校验场景）
     */
    public static LoginUser getLoginUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        return null;
    }

    /**
     * 获取当前登录用户，未登录直接抛异常
     */
    public static LoginUser getLoginUser() {
        LoginUser loginUser = getLoginUserOrNull();
        if (loginUser == null) {
            throw new ServiceException(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    public static Long getUserIdOrNull() {
        LoginUser loginUser = getLoginUserOrNull();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static String getUsernameOrNull() {
        LoginUser loginUser = getLoginUserOrNull();
        return loginUser == null ? null : loginUser.getUsername();
    }

    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    public static Long getDeptIdOrNull() {
        LoginUser loginUser = getLoginUserOrNull();
        return loginUser == null ? null : loginUser.getDeptId();
    }

    public static Long getDeptId() {
        return getLoginUser().getDeptId();
    }

    /**
     * 当前登录用户是否为超级管理员
     */
    public static boolean isAdmin() {
        Long userId = getUserIdOrNull();
        return isAdmin(userId);
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && Constants.ADMIN_USER_ID.equals(userId);
    }

    /**
     * 判断当前用户是否拥有指定权限
     */
    public static boolean hasPermission(String permission) {
        LoginUser loginUser = getLoginUserOrNull();
        if (loginUser == null || permission == null || permission.isEmpty()) {
            return false;
        }
        Set<String> permissions = loginUser.getPermissions();
        return permissions != null
                && (permissions.contains(Constants.ALL_PERMISSION) || permissions.contains(permission));
    }
}
