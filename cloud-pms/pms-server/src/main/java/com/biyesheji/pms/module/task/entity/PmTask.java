package com.biyesheji.pms.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.biyesheji.pms.common.core.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_task")
@Schema(description = "任务信息")
public class PmTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    @Schema(description = "项目ID")
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    @Schema(description = "父任务ID(0为顶级)")
    private Long parentId;

    @Schema(description = "所属里程碑ID")
    private Long milestoneId;

    @Schema(description = "任务名称")
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "优先级(1高 2中 3低)")
    private String priority;

    @Schema(description = "状态(0待开始 1进行中 2已完成 3已挂起)")
    private String status;

    @Schema(description = "进度百分比")
    private Integer progress;

    @Schema(description = "计划开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planStart;

    @Schema(description = "计划结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEnd;

    @Schema(description = "实际完成日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEnd;

    @Schema(description = "预估工时")
    private BigDecimal estimateHours;

    @Schema(description = "实际工时")
    private BigDecimal actualHours;

    @Schema(description = "排序号")
    private Integer orderNum;

    @JsonIgnore
    private String delFlag;

    @Schema(description = "备注")
    private String remark;

    /* ========== 非表字段 ========== */

    @Schema(description = "负责人姓名")
    @TableField(exist = false)
    private String assigneeName;

    @Schema(description = "项目名称")
    @TableField(exist = false)
    private String projectName;

    @Schema(description = "里程碑名称")
    @TableField(exist = false)
    private String milestoneName;

    @Schema(description = "是否已逾期")
    @TableField(exist = false)
    private Boolean overdue;

    @Schema(description = "子任务")
    @TableField(exist = false)
    private List<PmTask> children = new ArrayList<>();
}
