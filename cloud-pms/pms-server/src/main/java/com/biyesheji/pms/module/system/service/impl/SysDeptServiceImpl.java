package com.biyesheji.pms.module.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.module.system.entity.SysDept;
import com.biyesheji.pms.module.system.mapper.SysDeptMapper;
import com.biyesheji.pms.module.system.mapper.SysUserMapper;
import com.biyesheji.pms.module.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    /** 部门树最大层级，防止脏数据造成无限递归 */
    private static final int MAX_TREE_DEPTH = 20;

    private final SysUserMapper sysUserMapper;

    @Override
    public List<SysDept> selectDeptList(SysDept dept) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        if (dept != null) {
            if (StrUtil.isNotBlank(dept.getDeptName())) {
                wrapper.like(SysDept::getDeptName, dept.getDeptName());
            }
            if (StrUtil.isNotBlank(dept.getStatus())) {
                wrapper.eq(SysDept::getStatus, dept.getStatus());
            }
            if (dept.getParentId() != null) {
                wrapper.eq(SysDept::getParentId, dept.getParentId());
            }
        }
        wrapper.orderByAsc(SysDept::getParentId).orderByAsc(SysDept::getOrderNum);
        return list(wrapper);
    }

    @Override
    public List<SysDept> buildDeptTree(List<SysDept> depts) {
        if (depts == null || depts.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysDept> tree = new ArrayList<>();
        for (SysDept dept : depts) {
            if (Constants.ROOT_PARENT_ID.equals(dept.getParentId())) {
                fillChildren(dept, depts, 1);
                tree.add(dept);
            }
        }
        tree.sort(Comparator.comparing(SysDept::getOrderNum,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return tree;
    }

    private void fillChildren(SysDept parent, List<SysDept> all, int depth) {
        if (depth > MAX_TREE_DEPTH) {
            return;
        }
        List<SysDept> children = all.stream()
                .filter(d -> parent.getDeptId().equals(d.getParentId()))
                .sorted(Comparator.comparing(SysDept::getOrderNum,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        for (SysDept child : children) {
            fillChildren(child, all, depth + 1);
        }
        parent.setChildren(children);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertDept(SysDept dept) {
        if (Constants.ROOT_PARENT_ID.equals(dept.getParentId())) {
            dept.setAncestors(String.valueOf(Constants.ROOT_PARENT_ID));
        } else {
            SysDept parent = getById(dept.getParentId());
            if (parent == null) {
                throw new ServiceException("上级部门不存在");
            }
            if (Constants.STATUS_DISABLE.equals(parent.getStatus())) {
                throw new ServiceException("上级部门已停用，不允许新增下级");
            }
            dept.setAncestors(parent.getAncestors() + Constants.ANCESTORS_SEPARATOR + parent.getDeptId());
        }
        return save(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDept(SysDept dept) {
        SysDept old = getById(dept.getDeptId());
        if (old == null) {
            throw new ServiceException("部门不存在");
        }
        if (dept.getParentId() != null && !dept.getParentId().equals(old.getParentId())) {
            if (dept.getDeptId().equals(dept.getParentId())) {
                throw new ServiceException("上级部门不能是自己");
            }
            // 不能把部门挂到自己的子部门下
            List<Long> childIds = selectChildDeptIds(dept.getDeptId());
            if (childIds.contains(dept.getParentId())) {
                throw new ServiceException("上级部门不能是自己的下级部门");
            }
            SysDept parent = getById(dept.getParentId());
            if (parent == null) {
                throw new ServiceException("上级部门不存在");
            }
            String newAncestors = parent.getAncestors() + Constants.ANCESTORS_SEPARATOR + parent.getDeptId();
            refreshChildrenAncestors(dept.getDeptId(), old.getAncestors(), newAncestors);
            dept.setAncestors(newAncestors);
        }
        return updateById(dept);
    }

    /**
     * 父级变更后，级联刷新所有子部门的祖级列表
     */
    private void refreshChildrenAncestors(Long deptId, String oldAncestors, String newAncestors) {
        List<SysDept> children = baseMapper.selectChildrenByDeptId(deptId);
        if (children == null || children.isEmpty()) {
            return;
        }
        for (SysDept child : children) {
            String ancestors = child.getAncestors().replaceFirst("^" + oldAncestors, newAncestors);
            baseMapper.updateAncestors(child.getDeptId(), ancestors);
        }
    }

    @Override
    public void checkDeptCanDelete(Long deptId) {
        if (baseMapper.countChildren(deptId) > 0) {
            throw new ServiceException(ResultCode.DEPT_HAS_CHILDREN);
        }
        if (sysUserMapper.countByDeptId(deptId) > 0) {
            throw new ServiceException(ResultCode.DEPT_HAS_USER);
        }
    }

    @Override
    public List<Long> selectChildDeptIds(Long deptId) {
        List<Long> ids = new ArrayList<>();
        ids.add(deptId);
        List<SysDept> children = baseMapper.selectChildrenByDeptId(deptId);
        if (children != null) {
            for (SysDept child : children) {
                ids.add(child.getDeptId());
            }
        }
        return ids;
    }
}
