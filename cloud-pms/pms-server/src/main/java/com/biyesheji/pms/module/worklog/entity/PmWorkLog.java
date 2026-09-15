package com.biyesheji.pms.module.worklog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工时实体
 * <p>
 * 注意：pm_work_log 表只有 create_time/update_time，没有 create_by/update_by，
 * 因此不继承 BaseEntity，避免映射到不存在的列。
 */
@Data
@TableName("pm_work_log")
@Schema(description = "工时记录")
public class PmWorkLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "工时ID")
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    @Schema(description = "项目ID")
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "填报人ID")
    private Long userId;

    @Schema(description = "工作日期")
    @NotNull(message = "工作日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    @Schema(description = "工时数")
    @NotNull(message = "工时数不能为空")
    private BigDecimal hours;

    @Schema(description = "工作内容")
    private String content;

    @Schema(description = "状态(0待审批 1已通过 2已驳回)")
    private String status;

    @Schema(description = "审批人ID")
    private Long auditBy;

    @Schema(description = "审批时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    @Schema(description = "审批意见")
    private String auditRemark;

    @JsonIgnore
    private String delFlag;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /* ========== 非表字段 ========== */

    @Schema(description = "项目名称")
    @TableField(exist = false)
    private String projectName;

    @Schema(description = "任务名称")
    @TableField(exist = false)
    private String taskName;

    @Schema(description = "填报人昵称")
    @TableField(exist = false)
    private String nickName;

    @Schema(description = "审批人昵称")
    @TableField(exist = false)
    private String auditByName;

    @Schema(description = "批量操作的ID数组")
    @TableField(exist = false)
    private Long[] logIds;
}
