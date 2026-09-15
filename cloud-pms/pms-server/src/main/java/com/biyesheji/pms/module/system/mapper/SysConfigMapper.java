package com.biyesheji.pms.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.system.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 参数配置 Mapper
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {

    @Select("select config_value from sys_config where config_key = #{configKey}")
    String selectValueByKey(@Param("configKey") String configKey);
}
