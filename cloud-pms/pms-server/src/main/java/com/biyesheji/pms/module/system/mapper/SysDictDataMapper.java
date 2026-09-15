package com.biyesheji.pms.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.system.entity.SysDictData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 字典数据 Mapper
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    /**
     * 按字典类型批量删除
     */
    @Delete("delete from sys_dict_data where dict_type = #{dictType}")
    int deleteByType(@Param("dictType") String dictType);
}
