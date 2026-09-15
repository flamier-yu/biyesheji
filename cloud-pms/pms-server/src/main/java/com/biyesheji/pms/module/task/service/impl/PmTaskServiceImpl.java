package com.biyesheji.pms.module.task.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.framework.security.SecurityUtils;
import com.biyesheji.pms.module.project.service.IPmProjectService;
import com.biyesheji.pms.module.system.entity.SysUser;
import com.biyesheji.pms.module.system.mapper.SysUserMapper;
import com.biyesheji.pms.module.task.entity.PmTask;
import com.biyesheji.pms.module.task.entity.PmTaskComment;
import com.biyesheji.pms.module.task.mapper.PmTaskCommentMapper;
import com.biyesheji.pms.module.task.mapper.PmTaskMapper;
import com.biyesheji.pms.module.task.service.IPmTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 任务服务实现
 */
@Service
@RequiredArgsConstructor
public class PmTaskServiceImpl extends ServiceImpl<PmTaskMapper, PmTask> implements IPmTaskService {

    /** 任务状态：已完成 */
    private static final String STATUS_DONE = "2";
    /** 任务状态：进行中 */
    private static final String STATUS_RUNNING = "1";
    /** 任务状态：待开始 */
    private static final String STATUS_TODO = "0";
    /** 任务状态：已挂起 */
    private static final String STATUS_HOLD = "3";

    private static final int MAX_TREE_DEPTH = 10;

    private final PmTaskCommentMapper commentMapper;
    private final SysUserMapper sysUserMapper;
    private final IPmProjectService projectService;

    @Override
    public PageResult<PmTask> selectTaskPage(PmTask task, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<PmTask> wrapper = new LambdaQueryWrapper<>();
        if (task != null) {
            if (StrUtil.isNotBlank(task.getTaskName())) {
                wrapper.like(PmTask::getTaskName, task.getTaskName());
            }
            if (task.getProjectId() != null) {
                wrapper.eq(PmTask::getProjectId, task.getProjectId());
            }
            if (task.getAssigneeId() != null) {
                wrapper.eq(PmTask::getAssigneeId, task.getAssigneeId());
            }
            if (StrUtil.isNotBlank(task.getStatus())) {
                wrapper.eq(PmTask::getStatus, task.getStatus());
            }
            if (StrUtil.isNotBlank(task.getPriority())) {
                wrapper.eq(PmTask::getPriority, task.getPriority());
            }
        }
        wrapper.orderByAsc(PmTask::getOrderNum).orderByDesc(PmTask::getTaskId);
        Page<PmTask> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillExtra(page.getRecords());
        return PageResult.of(page);
    }

    /**
     * 批量填充负责人姓名与逾期标记
     */
    private void fillExtra(List<PmTask> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<Long> userIds = list.stream().map(PmTask::getAssigneeId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMap = sysUserMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName, (a, b) -> a));
        }
        for (PmTask t : list) {
            t.setAssigneeName(userMap.get(t.getAssigneeId()));
            t.setOverdue(isOverdue(t));
        }
    }

    private boolean isOverdue(PmTask task) {
        return task.getPlanEnd() != null
                && !STATUS_DONE.equals(task.getStatus())
                && task.getPlanEnd().isBefore(LocalDate.now());
    }

    @Override
    public List<PmTask> selectTaskTreeByProject(Long projectId) {
        List<PmTask> all = baseMapper.selectByProjectId(projectId);
        if (CollUtil.isEmpty(all)) {
            return new ArrayList<>();
        }
        fillExtra(all);

        List<PmTask> tree = new ArrayList<>();
        for (PmTask task : all) {
            if (task.getParentId() == null || Constants.ROOT_PARENT_ID.equals(task.getParentId())) {
                fillChildren(task, all, 1);
                tree.add(task);
            }
        }
        tree.sort(Comparator.comparing(PmTask::getOrderNum,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return tree;
    }

    private void fillChildren(PmTask parent, List<PmTask> all, int depth) {
        if (depth > MAX_TREE_DEPTH) {
            return;
        }
        List<PmTask> children = all.stream()
                .filter(t -> parent.getTaskId().equals(t.getParentId()))
                .sorted(Comparator.comparing(PmTask::getOrderNum,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        for (PmTask child : children) {
            fillChildren(child, all, depth + 1);
        }
        parent.setChildren(children);
    }

    @Override
    public List<PmTask> selectMyTasks() {
        Long userId = SecurityUtils.getUserIdOrNull();
        List<PmTask> list = baseMapper.selectByAssigneeId(userId);
        fillExtra(list);
        return list;
    }

    @Override
    public List<PmTask> selectBoardData(Long projectId) {
        // 看板只需要顶级任务，子任务在卡片内展示
        return selectTaskTreeByProject(projectId);
    }

    @Override
    public boolean insertTask(PmTask task) {
        if (task.getParentId() == null) {
            task.setParentId(Constants.ROOT_PARENT_ID);
        }
        if (task.getStatus() == null) {
            task.setStatus(STATUS_TODO);
        }
        if (task.getProgress() == null) {
            task.setProgress(0);
        }
        boolean saved = save(task);
        if (saved) {
            projectService.recalcProgress(task.getProjectId());
        }
        return saved;
    }

    @Override
    public boolean updateTask(PmTask task) {
        Long taskId = task.getTaskId();
        if (taskId == null) {
            throw new ServiceException("任务ID不能为空");
        }
        // 完成状态与进度保持一致
        if (STATUS_DONE.equals(task.getStatus())) {
            task.setProgress(100);
            if (task.getActualEnd() == null) {
                task.setActualEnd(LocalDate.now());
            }
        }
        boolean updated = updateById(task);
        if (updated) {
            projectService.recalcProgress(task.getProjectId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTaskByIds(Long[] taskIds) {
        if (taskIds == null || taskIds.length == 0) {
            return false;
        }
        Long projectId = getById(taskIds[0]).getProjectId();
        for (Long taskId : taskIds) {
            if (baseMapper.countChildren(taskId) > 0) {
                throw new ServiceException("任务存在子任务，请先删除子任务");
            }
            commentMapper.deleteByTaskId(taskId);
        }
        boolean removed = removeByIds(Arrays.asList(taskIds));
        if (removed) {
            projectService.recalcProgress(projectId);
        }
        return removed;
    }

    @Override
    public boolean changeStatus(Long taskId, String status, Integer progress) {
        PmTask task = getById(taskId);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        PmTask update = new PmTask();
        update.setTaskId(taskId);
        update.setStatus(status);
        if (STATUS_DONE.equals(status)) {
            update.setProgress(100);
            update.setActualEnd(LocalDate.now());
        } else if (progress != null) {
            update.setProgress(progress);
        } else if (STATUS_RUNNING.equals(status) && (task.getProgress() == null || task.getProgress() == 0)) {
            update.setProgress(10);
        }
        boolean ok = updateById(update);
        if (ok) {
            projectService.recalcProgress(task.getProjectId());
        }
        return ok;
    }

    @Override
    public boolean assignTask(Long taskId, Long assigneeId) {
        PmTask update = new PmTask();
        update.setTaskId(taskId);
        update.setAssigneeId(assigneeId);
        return updateById(update);
    }

    /* ==================== 评论 ==================== */

    @Override
    public List<PmTaskComment> selectComments(Long taskId) {
        return commentMapper.selectByTaskId(taskId);
    }

    @Override
    public boolean addComment(PmTaskComment comment) {
        comment.setUserId(SecurityUtils.getUserId());
        comment.setCreateTime(LocalDateTime.now());
        return commentMapper.insert(comment) > 0;
    }

    @Override
    public boolean deleteComment(Long commentId) {
        return commentMapper.deleteById(commentId) > 0;
    }
}
