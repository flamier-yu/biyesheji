package com.biyesheji.pms.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biyesheji.pms.module.system.entity.SysDept;

import java.util.List;

/**
 * 部门服务
 */
public interface ISysDeptService extends IService<SysDept> {

    /**
     * 条件查询部门列表（平铺）
     */
    List<SysDept> selectDeptList(SysDept dept);

    /**
     * 构建部门树
     */
    List<SysDept> buildDeptTree(List<SysDept> depts);

    /**
     * 新增部门（自动维护祖级列表）
     */
    boolean insertDept(SysDept dept);

    /**
     * 修改部门（父级变更时级联刷新子部门祖级列表）
     */
    boolean updateDept(SysDept dept);

    /**
     * 删除前的业务校验
     */
    void checkDeptCanDelete(Long deptId);

    /**
     * 查询部门下所有子部门ID（含自身）
     */
    List<Long> selectChildDeptIds(Long deptId);
}
