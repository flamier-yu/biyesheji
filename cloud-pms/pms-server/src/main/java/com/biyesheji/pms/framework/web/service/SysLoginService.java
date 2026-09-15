package com.biyesheji.pms.framework.web.service;

import cn.hutool.core.util.StrUtil;
import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.common.utils.ServletUtils;
import com.biyesheji.pms.framework.security.LoginUser;
import com.biyesheji.pms.framework.security.TokenService;
import com.biyesheji.pms.module.system.domain.LoginBody;
import com.biyesheji.pms.module.system.entity.SysLoginLog;
import com.biyesheji.pms.module.system.mapper.SysLoginLogMapper;
import com.biyesheji.pms.module.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 登录服务
 * <p>
 * 负责：验证码校验 → 密码认证 → 记录登录日志 → 签发令牌。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginService {

    /** 登录成功 */
    private static final String LOGIN_SUCCESS = "0";
    /** 登录失败 */
    private static final String LOGIN_FAIL = "1";

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ISysUserService sysUserService;
    private final SysLoginLogMapper sysLoginLogMapper;

    @Value("${pms.captcha-enabled:true}")
    private boolean captchaEnabled;

    /**
     * 账号密码登录，返回访问令牌
     */
    public String login(LoginBody loginBody) {
        String username = loginBody.getUsername();

        // 1. 校验图形验证码
        try {
            validateCaptcha(username, loginBody.getCode(), loginBody.getUuid());
        } catch (ServiceException e) {
            recordLoginLog(username, LOGIN_FAIL, e.getMessage());
            throw e;
        }

        // 2. 交给 Spring Security 完成密码比对
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginBody.getPassword()));
        } catch (DisabledException e) {
            recordLoginLog(username, LOGIN_FAIL, "账号已停用");
            throw new ServiceException(ResultCode.USER_DISABLED);
        } catch (AuthenticationException e) {
            log.warn("LOGIN FAILED. type={}, msg={}, username={}",
                    e.getClass().getName(), e.getMessage(), username);
            recordLoginLog(username, LOGIN_FAIL, "账号或密码错误");
            throw new ServiceException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 3. 记录登录信息并签发令牌
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String ip = getClientIp();
        sysUserService.updateLoginInfo(loginUser.getUserId(), ip);
        recordLoginLog(username, LOGIN_SUCCESS, "登录成功");
        log.info("用户登录成功：{}，IP：{}", username, ip);
        return tokenService.createToken(loginUser);
    }

    /**
     * 校验图形验证码。无论成功失败都删除缓存，防止被重复使用。
     */
    private void validateCaptcha(String username, String code, String uuid) {
        if (!captchaEnabled) {
            return;
        }
        if (StrUtil.isBlank(code) || StrUtil.isBlank(uuid)) {
            throw new ServiceException(ResultCode.CAPTCHA_ERROR);
        }
        String key = Constants.CAPTCHA_KEY + uuid;
        Object cached = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);

        if (cached == null) {
            log.warn("验证码已过期，username={}", username);
            throw new ServiceException(ResultCode.CAPTCHA_EXPIRED);
        }
        if (!code.trim().equalsIgnoreCase(cached.toString())) {
            log.warn("验证码错误，username={}", username);
            throw new ServiceException(ResultCode.CAPTCHA_ERROR);
        }
    }

    /**
     * 记录登录日志（失败不影响主流程）
     */
    private void recordLoginLog(String username, String status, String msg) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUsername(username);
            loginLog.setStatus(status);
            loginLog.setMsg(StrUtil.sub(msg, 0, 250));
            loginLog.setLoginTime(LocalDateTime.now());

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                loginLog.setIpaddr(ServletUtils.getClientIp(request));
                String userAgent = request.getHeader("User-Agent");
                loginLog.setBrowser(ServletUtils.getBrowser(userAgent));
                loginLog.setOs(ServletUtils.getOs(userAgent));
            }
            sysLoginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? Constants.UNKNOWN_IP
                : ServletUtils.getClientIp(attributes.getRequest());
    }
}
