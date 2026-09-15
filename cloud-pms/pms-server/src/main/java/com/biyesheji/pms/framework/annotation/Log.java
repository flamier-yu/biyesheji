package com.biyesheji.pms.framework.annotation;

import com.biyesheji.pms.common.enums.BusinessType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 方法上，由 LogAspect 统一记录操作日志，
 * 业务代码无需关心日志落库。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 模块标题 */
    String title() default "";

    /** 业务类型 */
    BusinessType businessType() default BusinessType.OTHER;

    /** 是否记录请求参数 */
    boolean saveRequestData() default true;

    /** 是否记录返回结果 */
    boolean saveResponseData() default true;
}
