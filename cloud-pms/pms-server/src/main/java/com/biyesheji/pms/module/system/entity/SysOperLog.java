package com.biyesheji.pms.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_oper_log")
@Schema(description = "操作日志")
public class SysOperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志主键")
    @TableId(value = "oper_id", type = IdType.AUTO)
    private Long operId;

    @Schema(description = "模块标题")
    private String title;

    @Schema(description = "业务类型(0其它 1新增 2修改 3删除 4导出 5导入)")
    private Integer businessType;

    @Schema(description = "方法名称")
    private String method;

    @Schema(description = "请求方式")
    private String requestMethod;

    @Schema(description = "操作人员")
    private String operName;

    @Schema(description = "请求URL")
    private String operUrl;

    @Schema(description = "操作IP")
    private String operIp;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "返回结果")
    private String jsonResult;

    @Schema(description = "操作状态(0正常 1异常)")
    private Integer status;

    @Schema(description = "错误消息")
    private String errorMsg;

    @Schema(description = "消耗时间(毫秒)")
    private Long costTime;

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;
}
