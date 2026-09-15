package com.biyesheji.pms.framework.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.biyesheji.pms.common.utils.ServletUtils;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.framework.security.SecurityUtils;
import com.biyesheji.pms.module.system.entity.SysOperLog;
import com.biyesheji.pms.module.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 操作日志切面
 * <p>
 * 拦截标注了 @Log 的方法，记录操作人、请求参数、返回结果、耗时与异常信息。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    /** 敏感字段脱敏正则：把 password 类字段的值替换为 ****** */
    private static final Pattern SENSITIVE_PATTERN =
            Pattern.compile("(\"[A-Za-z]*[Pp]assword[A-Za-z]*\"\\s*:\\s*)\"[^\"]*\"");

    private static final int MAX_LENGTH = 1900;

    private final SysOperLogMapper operLogMapper;

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult, 0L);
    }

    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null, 0L);
    }

    private void handleLog(JoinPoint joinPoint, Log controllerLog, Exception e, Object jsonResult, long cost) {
        try {
            SysOperLog operLog = new SysOperLog();
            operLog.setTitle(controllerLog.title());
            operLog.setBusinessType(controllerLog.businessType().value());
            operLog.setStatus(e == null ? 0 : 1);
            operLog.setCostTime(cost);
            operLog.setOperTime(LocalDateTime.now());
            operLog.setOperName(SecurityUtils.getUsernameOrNull());

            if (e != null) {
                operLog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, MAX_LENGTH));
            }

            // 请求信息
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operLog.setOperIp(ServletUtils.getClientIp(request));
                operLog.setOperUrl(StrUtil.sub(request.getRequestURI(), 0, 250));
                operLog.setRequestMethod(request.getMethod());
            }

            // 目标方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            operLog.setMethod(StrUtil.sub(
                    signature.getDeclaringTypeName() + "." + method.getName() + "()", 0, 190));

            // 请求参数
            if (controllerLog.saveRequestData()) {
                operLog.setOperParam(StrUtil.sub(buildParams(joinPoint), 0, MAX_LENGTH));
            }

            // 返回结果
            if (controllerLog.saveResponseData() && jsonResult != null) {
                operLog.setJsonResult(StrUtil.sub(safeToJson(jsonResult), 0, MAX_LENGTH));
            }

            operLogMapper.insert(operLog);
        } catch (Exception ex) {
            // 记日志失败不能影响主流程
            log.error("记录操作日志失败", ex);
        }
    }

    /**
     * 序列化方法入参，过滤掉 Servlet 与文件类型，并对敏感字段脱敏
     */
    private String buildParams(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }
        List<Object> list = new ArrayList<>(args.length);
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg instanceof HttpServletRequest
                    || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile
                    || arg instanceof MultipartFile[]) {
                continue;
            }
            list.add(arg);
        }
        String json = safeToJson(list);
        return SENSITIVE_PATTERN.matcher(json).replaceAll("$1\"******\"");
    }

    private String safeToJson(Object obj) {
        try {
            return JSONUtil.toJsonStr(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
