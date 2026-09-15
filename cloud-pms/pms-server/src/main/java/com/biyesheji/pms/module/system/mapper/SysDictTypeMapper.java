package com.biyesheji.pms.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.system.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 字典类型 Mapper
 */
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

    @Select("select count(1) from sys_dict_data where dict_type = #{dictType}")
    int countDataByType(@Param("dictType") String dictType);
}
