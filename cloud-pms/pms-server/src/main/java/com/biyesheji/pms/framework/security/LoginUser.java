package com.biyesheji.pms.framework.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 登录用户身份信息
 * <p>
 * 认证通过后放入 SecurityContext，同时缓存到 Redis 支持无状态鉴权。
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 部门ID */
    private Long deptId;

    /** 登录账号 */
    private String username;

    /** 用户昵称 */
    private String nickName;

    /** 密码（仅认证阶段使用，不参与序列化） */
    @JsonIgnore
    private String password;

    /** 头像 */
    private String avatar;

    /** 唯一令牌标识 */
    private String token;

    /** 登录时间 */
    private Long loginTime;

    /** 过期时间 */
    private Long expireTime;

    /** 权限标识集合 */
    private Set<String> permissions = new HashSet<>();

    /** 角色标识集合 */
    private Set<String> roles = new HashSet<>();

    public LoginUser(Long userId, Long deptId, String username, String nickName, String password) {
        this.userId = userId;
        this.deptId = deptId;
        this.username = username;
        this.nickName = nickName;
        this.password = password;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (permissions != null) {
            for (String permission : permissions) {
                if (permission != null && !permission.trim().isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(permission.trim()));
                }
            }
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
