package com.biyesheji.pms.module.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目成员实体
 */
@Data
@TableName("pm_project_member")
@Schema(description = "项目成员")
public class PmProjectMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "项目内角色")
    private String roleInPj;

    @Schema(description = "加入时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /* ========== 非表字段 ========== */

    @Schema(description = "用户昵称")
    @TableField(exist = false)
    private String nickName;

    @Schema(description = "登录账号")
    @TableField(exist = false)
    private String username;
}
