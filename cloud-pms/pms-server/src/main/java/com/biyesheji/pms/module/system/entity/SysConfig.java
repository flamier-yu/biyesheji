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
 * 参数配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
@Schema(description = "参数配置")
public class SysConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "参数主键")
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    @Schema(description = "参数名称")
    @NotBlank(message = "参数名称不能为空")
    @Size(max = 100, message = "参数名称长度不能超过100个字符")
    private String configName;

    @Schema(description = "参数键名")
    @NotBlank(message = "参数键名不能为空")
    @Size(max = 100, message = "参数键名长度不能超过100个字符")
    private String configKey;

    @Schema(description = "参数键值")
    @NotBlank(message = "参数键值不能为空")
    private String configValue;

    @Schema(description = "系统内置(Y是 N否)")
    private String configType;

    @Schema(description = "备注")
    private String remark;
}
