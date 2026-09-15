package com.biyesheji.pms.module.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
import java.time.LocalDate;

/**
 * 里程碑实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_milestone")
@Schema(description = "项目里程碑")
public class PmMilestone extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "里程碑ID")
    @TableId(value = "milestone_id", type = IdType.AUTO)
    private Long milestoneId;

    @Schema(description = "项目ID")
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    @Schema(description = "里程碑名称")
    @NotBlank(message = "里程碑名称不能为空")
    private String milestoneName;

    @Schema(description = "计划完成日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    @Schema(description = "实际完成日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualDate;

    @Schema(description = "状态(0未开始 1进行中 2已完成 3已延期)")
    private String status;

    @Schema(description = "显示顺序")
    private Integer orderNum;

    @JsonIgnore
    private String delFlag;

    @Schema(description = "备注")
    private String remark;
}
