package com.biyesheji.pms.module.file.entity;

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
 * 附件实体
 * <p>
 * 表内没有 create_by/update_by，故不继承 BaseEntity。
 */
@Data
@TableName("pm_attachment")
@Schema(description = "附件信息")
public class PmAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "附件ID")
    @TableId(value = "attach_id", type = IdType.AUTO)
    private Long attachId;

    @Schema(description = "业务类型(project/task/worklog)")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "存储路径")
    private String filePath;

    @Schema(description = "文件后缀")
    private String fileSuffix;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件MD5")
    private String fileMd5;

    @Schema(description = "上传人ID")
    private Long uploadBy;

    @JsonIgnore
    private String delFlag;

    @Schema(description = "上传时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /* ========== 非表字段 ========== */

    @Schema(description = "上传人昵称")
    @TableField(exist = false)
    private String uploadByName;

    @Schema(description = "可访问地址")
    @TableField(exist = false)
    private String url;

    @Schema(description = "文件大小(可读)")
    @TableField(exist = false)
    private String sizeText;
}
