package com.biyesheji.pms.module.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目燃尽图快照实体
 */
@Data
@TableName("pm_project_burndown")
@Schema(description = "燃尽图快照")
public class PmProjectBurndown implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "快照ID")
    @TableId(value = "snapshot_id", type = IdType.AUTO)
    private Long snapshotId;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "快照日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate snapshotDate;

    @Schema(description = "总预估工时")
    private BigDecimal totalHours;

    @Schema(description = "剩余工时")
    private BigDecimal remainingHours;

    @Schema(description = "已完成工时")
    private BigDecimal completedHours;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
