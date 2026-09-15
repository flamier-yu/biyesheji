package com.biyesheji.pms.framework.security;

import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.utils.ServletUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限不足处理：返回统一 JSON
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        ServletUtils.renderJson(response, R.fail(ResultCode.FORBIDDEN), HttpServletResponse.SC_OK);
    }
}
