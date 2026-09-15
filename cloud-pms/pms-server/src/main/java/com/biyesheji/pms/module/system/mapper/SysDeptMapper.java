package com.biyesheji.pms.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.system.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 部门 Mapper
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 统计子部门数量
     */
    @Select("select count(1) from sys_dept where parent_id = #{deptId} and del_flag = '0'")
    int countChildren(@Param("deptId") Long deptId);

    /**
     * 查询某部门下的所有子部门（按祖级列表匹配）
     */
    @Select("select * from sys_dept where del_flag = '0' and find_in_set(#{deptId}, ancestors)")
    List<SysDept> selectChildrenByDeptId(@Param("deptId") Long deptId);

    /**
     * 同步子部门的祖级列表
     */
    @Update("update sys_dept set ancestors = #{ancestors}, update_time = now() where dept_id = #{deptId}")
    int updateAncestors(@Param("deptId") Long deptId, @Param("ancestors") String ancestors);
}
