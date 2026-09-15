package com.biyesheji.pms.common.utils;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.biyesheji.pms.common.constant.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet 相关工具
 */
public final class ServletUtils {

    private ServletUtils() {
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * 把对象以 JSON 形式写回响应
     */
    public static void renderJson(HttpServletResponse response, Object data, int status) {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(OBJECT_MAPPER.writeValueAsString(data));
            writer.flush();
        } catch (IOException e) {
            // 客户端可能已断开，忽略
        }
    }

    /**
     * 获取客户端真实 IP（考虑常见代理头）
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return Constants.UNKNOWN_IP;
        }
        String[] headers = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                int idx = ip.indexOf(',');
                return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
            }
        }
        String remote = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(remote) ? "127.0.0.1" : remote;
    }

    /**
     * 从 User-Agent 中粗略识别浏览器
     */
    public static String getBrowser(String userAgent) {
        if (StrUtil.isBlank(userAgent)) {
            return "未知";
        }
        if (userAgent.contains("Edg")) {
            return "Edge";
        }
        if (userAgent.contains("Chrome")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari")) {
            return "Safari";
        }
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "IE";
        }
        return "其它";
    }

    /**
     * 从 User-Agent 中粗略识别操作系统
     */
    public static String getOs(String userAgent) {
        if (StrUtil.isBlank(userAgent)) {
            return "未知";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("mac os")) {
            return "macOS";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("iphone") || ua.contains("ipad")) {
            return "iOS";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }
        return "其它";
    }
}
