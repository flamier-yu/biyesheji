package com.biyesheji.pms.framework.security;

import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.utils.ServletUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 未认证处理：返回统一 JSON，而不是 Spring Security 默认的登录页跳转
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) {
        ServletUtils.renderJson(response, R.fail(ResultCode.UNAUTHORIZED), HttpServletResponse.SC_OK);
    }
}
