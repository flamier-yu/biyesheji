package com.biyesheji.pms.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.biyesheji.pms.common.core.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * 字典数据实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
@Schema(description = "字典数据")
public class SysDictData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "字典编码")
    @TableId(value = "dict_code", type = IdType.AUTO)
    private Long dictCode;

    @Schema(description = "字典排序")
    private Integer dictSort;

    @Schema(description = "字典标签")
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    @Schema(description = "字典键值")
    @NotBlank(message = "字典键值不能为空")
    private String dictValue;

    @Schema(description = "字典类型")
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @Schema(description = "样式属性")
    private String cssClass;

    @Schema(description = "表格回显样式")
    private String listClass;

    @Schema(description = "是否默认(Y是 N否)")
    private String isDefault;

    @Schema(description = "状态(0正常 1停用)")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
