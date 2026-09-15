package com.biyesheji.pms.module.project.entity;

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
 * 项目实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_project")
@Schema(description = "项目信息")
public class PmProject extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID")
    @TableId(value = "project_id", type = IdType.AUTO)
    private Long projectId;

    @Schema(description = "项目编号")
    @NotBlank(message = "项目编号不能为空")
    private String projectCode;

    @Schema(description = "项目名称")
    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    @Schema(description = "项目经理ID")
    @NotNull(message = "项目经理不能为空")
    private Long managerId;

    @Schema(description = "所属部门ID")
    private Long deptId;

    @Schema(description = "项目描述")
    private String description;

    @Schema(description = "优先级(1高 2中 3低)")
    private String priority;

    @Schema(description = "状态(0待审批 1进行中 2已完成 3已暂停 4已终止)")
    private String status;

    @Schema(description = "进度百分比")
    private Integer progress;

    @Schema(description = "项目预算")
    private BigDecimal budget;

    @Schema(description = "计划开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "计划结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "实际结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEnd;

    @JsonIgnore
    private String delFlag;

    @Schema(description = "备注")
    private String remark;

    /* ========== 非表字段 ========== */

    @Schema(description = "项目经理姓名")
    @TableField(exist = false)
    private String managerName;

    @Schema(description = "所属部门名称")
    @TableField(exist = false)
    private String deptName;

    @Schema(description = "项目成员ID数组")
    @TableField(exist = false)
    private Long[] memberIds;

    @Schema(description = "项目成员")
    @TableField(exist = false)
    private List<PmProjectMember> members = new ArrayList<>();

    @Schema(description = "里程碑")
    @TableField(exist = false)
    private List<PmMilestone> milestones = new ArrayList<>();

    @Schema(description = "任务总数")
    @TableField(exist = false)
    private Integer taskCount;

    @Schema(description = "已完成任务数")
    @TableField(exist = false)
    private Integer doneTaskCount;
}
