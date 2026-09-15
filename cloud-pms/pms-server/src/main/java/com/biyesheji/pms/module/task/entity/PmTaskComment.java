package com.biyesheji.pms.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务评论实体
 */
@Data
@TableName("pm_task_comment")
@Schema(description = "任务评论")
public class PmTaskComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "评论ID")
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    @Schema(description = "任务ID")
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @Schema(description = "评论人ID")
    private Long userId;

    @Schema(description = "评论内容")
    @NotBlank(message = "评论内容不能为空")
    private String content;

    @JsonIgnore
    private String delFlag;

    @Schema(description = "评论时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /* ========== 非表字段 ========== */

    @Schema(description = "评论人昵称")
    @TableField(exist = false)
    private String nickName;
}
