package com.biyesheji.pms.module.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.framework.security.SecurityUtils;
import com.biyesheji.pms.module.project.entity.PmMilestone;
import com.biyesheji.pms.module.project.entity.PmProject;
import com.biyesheji.pms.module.project.entity.PmProjectMember;
import com.biyesheji.pms.module.project.mapper.PmMilestoneMapper;
import com.biyesheji.pms.module.project.mapper.PmProjectMapper;
import com.biyesheji.pms.module.project.mapper.PmProjectMemberMapper;
import com.biyesheji.pms.module.project.service.IPmProjectService;
import com.biyesheji.pms.module.system.entity.SysDept;
import com.biyesheji.pms.module.system.entity.SysUser;
import com.biyesheji.pms.module.system.mapper.SysDeptMapper;
import com.biyesheji.pms.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 项目服务实现
 */
@Service
@RequiredArgsConstructor
public class PmProjectServiceImpl extends ServiceImpl<PmProjectMapper, PmProject> implements IPmProjectService {

    private static final String STATUS_DONE = "2";

    private final PmProjectMemberMapper memberMapper;
    private final PmMilestoneMapper milestoneMapper;
    private final SysUserMapper sysUserMapper;
    private final SysDeptMapper sysDeptMapper;

    @Override
    public PageResult<PmProject> selectProjectPage(PmProject project, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<PmProject> wrapper = new LambdaQueryWrapper<>();
        if (project != null) {
            if (StrUtil.isNotBlank(project.getProjectName())) {
                wrapper.like(PmProject::getProjectName, project.getProjectName());
            }
            if (StrUtil.isNotBlank(project.getProjectCode())) {
                wrapper.like(PmProject::getProjectCode, project.getProjectCode());
            }
            if (StrUtil.isNotBlank(project.getStatus())) {
                wrapper.eq(PmProject::getStatus, project.getStatus());
            }
            if (StrUtil.isNotBlank(project.getPriority())) {
                wrapper.eq(PmProject::getPriority, project.getPriority());
            }
            if (project.getManagerId() != null) {
                wrapper.eq(PmProject::getManagerId, project.getManagerId());
            }
        }

        // 数据隔离：非管理员只能看到自己参与的项目
        if (!SecurityUtils.isAdmin()) {
            List<Long> myProjectIds = memberMapper.selectProjectIdsByUserId(SecurityUtils.getUserId());
            if (CollUtil.isEmpty(myProjectIds)) {
                return PageResult.empty();
            }
            wrapper.in(PmProject::getProjectId, myProjectIds);
        }

        wrapper.orderByDesc(PmProject::getProjectId);
        Page<PmProject> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillExtra(page.getRecords());
        return PageResult.of(page);
    }

    /**
     * 批量填充经理名、部门名与任务统计，避免 N+1 查询
     */
    private void fillExtra(List<PmProject> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<Long> userIds = list.stream().map(PmProject::getManagerId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMap = sysUserMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName, (a, b) -> a));
        }

        List<Long> deptIds = list.stream().map(PmProject::getDeptId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> deptMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            deptMap = sysDeptMapper.selectBatchIds(deptIds).stream()
                    .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName, (a, b) -> a));
        }

        for (PmProject p : list) {
            p.setManagerName(userMap.get(p.getManagerId()));
            p.setDeptName(deptMap.get(p.getDeptId()));
            p.setTaskCount(baseMapper.countTasks(p.getProjectId()));
            p.setDoneTaskCount(baseMapper.countDoneTasks(p.getProjectId()));
        }
    }

    @Override
    public PmProject selectProjectDetail(Long projectId) {
        PmProject project = getById(projectId);
        if (project == null) {
            throw new ServiceException(ResultCode.PROJECT_NOT_EXIST);
        }
        fillExtra(java.util.Collections.singletonList(project));
        project.setMembers(memberMapper.selectByProjectId(projectId));
        project.setMilestones(milestoneMapper.selectByProjectId(projectId));
        project.setMemberIds(project.getMembers().stream()
                .map(PmProjectMember::getUserId).toArray(Long[]::new));
        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertProject(PmProject project) {
        if (baseMapper.countByCode(project.getProjectCode()) > 0) {
            throw new ServiceException(ResultCode.PROJECT_CODE_EXIST);
        }
        if (project.getStatus() == null) {
            project.setStatus("0");
        }
        boolean saved = save(project);
        bindMembers(project.getProjectId(), project.getMemberIds(), project.getManagerId());
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProject(PmProject project) {
        Long projectId = project.getProjectId();
        if (projectId == null) {
            throw new ServiceException("项目ID不能为空");
        }
        if (StrUtil.isNotBlank(project.getProjectCode())) {
            PmProject exist = lambdaQuery().eq(PmProject::getProjectCode, project.getProjectCode()).one();
            if (exist != null && !exist.getProjectId().equals(projectId)) {
                throw new ServiceException(ResultCode.PROJECT_CODE_EXIST);
            }
        }
        boolean updated = updateById(project);
        if (project.getMemberIds() != null) {
            memberMapper.deleteByProjectId(projectId);
            bindMembers(projectId, project.getMemberIds(), project.getManagerId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProjectByIds(Long[] projectIds) {
        if (projectIds == null || projectIds.length == 0) {
            return false;
        }
        for (Long id : projectIds) {
            memberMapper.deleteByProjectId(id);
        }
        return removeByIds(Arrays.asList(projectIds));
    }

    @Override
    public boolean changeStatus(Long projectId, String status) {
        PmProject project = getById(projectId);
        if (project == null) {
            throw new ServiceException(ResultCode.PROJECT_NOT_EXIST);
        }
        PmProject update = new PmProject();
        update.setProjectId(projectId);
        update.setStatus(status);
        // 结项时补充实际结束日期
        if (STATUS_DONE.equals(status)) {
            update.setActualEnd(LocalDate.now());
            update.setProgress(100);
        }
        return updateById(update);
    }

    @Override
    public void recalcProgress(Long projectId) {
        int total = baseMapper.countTasks(projectId);
        int done = baseMapper.countDoneTasks(projectId);
        int progress = total == 0 ? 0 : (int) Math.round(done * 100.0 / total);

        PmProject update = new PmProject();
        update.setProjectId(projectId);
        update.setProgress(progress);
        if (progress == 100 && total > 0) {
            update.setStatus(STATUS_DONE);
            update.setActualEnd(LocalDate.now());
        }
        updateById(update);
    }

    @Override
    public List<PmProject> selectMyProjects() {
        Long userId = SecurityUtils.getUserIdOrNull();
        List<PmProject> list;
        if (SecurityUtils.isAdmin(userId)) {
            list = lambdaQuery().ne(PmProject::getStatus, "4")
                    .orderByDesc(PmProject::getProjectId).list();
        } else {
            List<Long> ids = memberMapper.selectProjectIdsByUserId(userId);
            if (CollUtil.isEmpty(ids)) {
                return new ArrayList<>();
            }
            list = lambdaQuery().in(PmProject::getProjectId, ids)
                    .ne(PmProject::getStatus, "4")
                    .orderByDesc(PmProject::getProjectId).list();
        }
        fillExtra(list);
        return list;
    }

    /* ==================== 里程碑 ==================== */

    @Override
    public List<PmMilestone> selectMilestones(Long projectId) {
        return milestoneMapper.selectByProjectId(projectId);
    }

    @Override
    public boolean saveMilestone(PmMilestone milestone) {
        if (milestone.getMilestoneId() == null) {
            if (milestone.getStatus() == null) {
                milestone.setStatus("0");
            }
            return milestoneMapper.insert(milestone) > 0;
        }
        return milestoneMapper.updateById(milestone) > 0;
    }

    @Override
    public boolean deleteMilestone(Long milestoneId) {
        return milestoneMapper.deleteById(milestoneId) > 0;
    }

    /**
     * 绑定项目成员：项目经理自动加入，其余成员按传入列表去重加入
     */
    private void bindMembers(Long projectId, Long[] memberIds, Long managerId) {
        List<Long> userIds = new ArrayList<>();
        if (managerId != null) {
            userIds.add(managerId);
        }
        if (memberIds != null) {
            for (Long id : memberIds) {
                if (id != null && !userIds.contains(id)) {
                    userIds.add(id);
                }
            }
        }
        if (!userIds.isEmpty()) {
            memberMapper.batchInsert(projectId, userIds, Constants.STATUS_NORMAL);
        }
    }
}
