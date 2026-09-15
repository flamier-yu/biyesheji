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
 * 登录日志实体
 */
@Data
@TableName("sys_login_log")
@Schema(description = "登录日志")
public class SysLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "访问ID")
    @TableId(value = "info_id", type = IdType.AUTO)
    private Long infoId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "登录IP")
    private String ipaddr;

    @Schema(description = "浏览器类型")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "登录状态(0成功 1失败)")
    private String status;

    @Schema(description = "提示消息")
    private String msg;

    @Schema(description = "访问时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}
