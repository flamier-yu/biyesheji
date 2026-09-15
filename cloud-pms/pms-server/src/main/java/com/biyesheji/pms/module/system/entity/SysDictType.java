package com.biyesheji.pms.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.biyesheji.pms.common.core.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 字典类型实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
@Schema(description = "字典类型")
public class SysDictType extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "字典主键")
    @TableId(value = "dict_id", type = IdType.AUTO)
    private Long dictId;

    @Schema(description = "字典名称")
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100个字符")
    private String dictName;

    @Schema(description = "字典类型")
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100个字符")
    private String dictType;

    @Schema(description = "状态(0正常 1停用)")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
