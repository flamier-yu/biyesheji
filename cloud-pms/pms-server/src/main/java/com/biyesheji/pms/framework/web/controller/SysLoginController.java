package com.biyesheji.pms.framework.web.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.framework.security.LoginUser;
import com.biyesheji.pms.framework.security.SecurityUtils;
import com.biyesheji.pms.framework.security.TokenService;
import com.biyesheji.pms.framework.web.service.SysLoginService;
import com.biyesheji.pms.module.system.domain.LoginBody;
import com.biyesheji.pms.module.system.domain.vo.CaptchaVo;
import com.biyesheji.pms.module.system.domain.vo.LoginVo;
import com.biyesheji.pms.module.system.domain.vo.RouterVo;
import com.biyesheji.pms.module.system.domain.vo.UserInfoVo;
import com.biyesheji.pms.module.system.entity.SysMenu;
import com.biyesheji.pms.module.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证相关接口
 */
@Tag(name = "01. 认证管理", description = "验证码、登录、登出、用户信息与动态路由")
@RestController
@RequiredArgsConstructor
@Validated
public class SysLoginController {

    /** 验证码有效期（分钟） */
    private static final long CAPTCHA_EXPIRE_MINUTES = 2L;

    private final SysLoginService loginService;
    private final TokenService tokenService;
    private final ISysMenuService sysMenuService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${pms.captcha-enabled:true}")
    private boolean captchaEnabled;

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public R<CaptchaVo> captcha() {
        if (!captchaEnabled) {
            return R.ok(new CaptchaVo(null, null, false));
        }
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 30);
        String uuid = IdUtil.fastSimpleUUID();
        redisTemplate.opsForValue().set(
                Constants.CAPTCHA_KEY + uuid,
                captcha.getCode(),
                CAPTCHA_EXPIRE_MINUTES,
                TimeUnit.MINUTES);
        return R.ok(new CaptchaVo(uuid, captcha.getImageBase64Data(), true));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginVo> login(@Valid @RequestBody LoginBody loginBody) {
        return R.ok(new LoginVo(loginService.login(loginBody)));
    }

    @Operation(summary = "获取当前登录用户信息与权限")
    @GetMapping("/getInfo")
    public R<UserInfoVo> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        UserInfoVo vo = new UserInfoVo();
        vo.setUserId(loginUser.getUserId());
        vo.setUsername(loginUser.getUsername());
        vo.setNickName(loginUser.getNickName());
        vo.setAvatar(loginUser.getAvatar());
        vo.setDeptId(loginUser.getDeptId());
        vo.setRoles(loginUser.getRoles());
        vo.setPermissions(loginUser.getPermissions());
        return R.ok(vo);
    }

    @Operation(summary = "获取动态路由菜单")
    @GetMapping("/getRouters")
    public R<List<RouterVo>> getRouters() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menuTree = sysMenuService.selectMenuTreeByUserId(userId);
        return R.ok(sysMenuService.buildMenus(menuTree));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        LoginUser loginUser = SecurityUtils.getLoginUserOrNull();
        if (loginUser != null) {
            tokenService.deleteToken(loginUser.getToken());
        }
        return R.ok("退出成功", null);
    }
}
