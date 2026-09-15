package com.biyesheji.pms.module.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息实体
 */
@Data
@TableName("pm_message")
@Schema(description = "站内消息")
public class PmMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID")
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    @Schema(description = "接收人ID")
    private Long receiverId;

    @Schema(description = "发送人ID")
    private Long senderId;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型(0系统 1任务 2项目 3工时 4预警)")
    private String msgType;

    @Schema(description = "关联业务类型")
    private String bizType;

    @Schema(description = "关联业务ID")
    private Long bizId;

    @Schema(description = "是否已读(0未读 1已读)")
    private String isRead;

    @Schema(description = "阅读时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    @JsonIgnore
    private String delFlag;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /* ========== 非表字段 ========== */

    @Schema(description = "发送人昵称")
    @TableField(exist = false)
    private String senderName;

    @Schema(description = "批量操作ID数组")
    @TableField(exist = false)
    private Long[] messageIds;
}
