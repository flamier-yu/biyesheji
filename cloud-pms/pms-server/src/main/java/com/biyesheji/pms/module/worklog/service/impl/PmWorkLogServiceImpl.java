package com.biyesheji.pms.module.worklog.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.framework.security.SecurityUtils;
import com.biyesheji.pms.module.project.entity.PmProject;
import com.biyesheji.pms.module.project.mapper.PmProjectMapper;
import com.biyesheji.pms.module.system.entity.SysUser;
import com.biyesheji.pms.module.system.mapper.SysUserMapper;
import com.biyesheji.pms.module.task.entity.PmTask;
import com.biyesheji.pms.module.task.mapper.PmTaskMapper;
import com.biyesheji.pms.module.worklog.entity.PmWorkLog;
import com.biyesheji.pms.module.worklog.mapper.PmWorkLogMapper;
import com.biyesheji.pms.module.worklog.service.IPmWorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工时服务实现
 */
@Service
@RequiredArgsConstructor
public class PmWorkLogServiceImpl extends ServiceImpl<PmWorkLogMapper, PmWorkLog>
        implements IPmWorkLogService {

    /** 单日填报工时上限 */
    private static final BigDecimal MAX_DAILY_HOURS = new BigDecimal("24");

    /** 工时状态 */
    private static final String STATUS_PENDING = "0";
    private static final String STATUS_PASSED = "1";
    private static final String STATUS_REJECTED = "2";

    private final SysUserMapper sysUserMapper;
    private final PmProjectMapper projectMapper;
    private final PmTaskMapper taskMapper;

    @Override
    public PageResult<PmWorkLog> selectWorkLogPage(PmWorkLog log, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<PmWorkLog> wrapper = new LambdaQueryWrapper<>();
        if (log != null) {
            if (log.getProjectId() != null) {
                wrapper.eq(PmWorkLog::getProjectId, log.getProjectId());
            }
            if (log.getUserId() != null) {
                wrapper.eq(PmWorkLog::getUserId, log.getUserId());
            }
            if (StrUtil.isNotBlank(log.getStatus())) {
                wrapper.eq(PmWorkLog::getStatus, log.getStatus());
            }
            if (log.getWorkDate() != null) {
                wrapper.eq(PmWorkLog::getWorkDate, log.getWorkDate());
            }
        }
        wrapper.orderByDesc(PmWorkLog::getWorkDate).orderByDesc(PmWorkLog::getLogId);
        Page<PmWorkLog> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillExtra(page.getRecords());
        return PageResult.of(page);
    }

    /**
     * 批量填充项目名、任务名、人员昵称
     */
    private void fillExtra(List<PmWorkLog> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<Long> projectIds = list.stream().map(PmWorkLog::getProjectId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> projectMap = new HashMap<>();
        if (!projectIds.isEmpty()) {
            projectMap = projectMapper.selectBatchIds(projectIds).stream()
                    .collect(Collectors.toMap(PmProject::getProjectId, PmProject::getProjectName, (a, b) -> a));
        }

        List<Long> taskIds = list.stream().map(PmWorkLog::getTaskId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> taskMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            taskMap = taskMapper.selectBatchIds(taskIds).stream()
                    .collect(Collectors.toMap(PmTask::getTaskId, PmTask::getTaskName, (a, b) -> a));
        }

        List<Long> userIds = list.stream().map(PmWorkLog::getUserId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMap = sysUserMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName, (a, b) -> a));
        }

        for (PmWorkLog w : list) {
            w.setProjectName(projectMap.get(w.getProjectId()));
            w.setTaskName(taskMap.get(w.getTaskId()));
            w.setNickName(userMap.get(w.getUserId()));
            w.setAuditByName(userMap.get(w.getAuditBy()));
        }
    }

    @Override
    public List<PmWorkLog> selectMyLogs(String startDate, String endDate) {
        Long userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<PmWorkLog> wrapper = new LambdaQueryWrapper<PmWorkLog>()
                .eq(PmWorkLog::getUserId, userId);
        if (StrUtil.isNotBlank(startDate)) {
            wrapper.ge(PmWorkLog::getWorkDate, startDate);
        }
        if (StrUtil.isNotBlank(endDate)) {
            wrapper.le(PmWorkLog::getWorkDate, endDate);
        }
        wrapper.orderByDesc(PmWorkLog::getWorkDate);
        List<PmWorkLog> list = list(wrapper);
        fillExtra(list);
        return list;
    }

    @Override
    public boolean insertWorkLog(PmWorkLog workLog) {
        workLog.setUserId(SecurityUtils.getUserId());
        workLog.setStatus(STATUS_PENDING);
        workLog.setCreateTime(LocalDateTime.now());
        checkDailyHours(workLog.getUserId(), workLog.getWorkDate(), workLog.getHours(), 0L);
        return save(workLog);
    }

    @Override
    public boolean updateWorkLog(PmWorkLog workLog) {
        PmWorkLog old = getById(workLog.getLogId());
        if (old == null) {
            throw new ServiceException("工时记录不存在");
        }
        if (STATUS_PASSED.equals(old.getStatus())) {
            throw new ServiceException("已通过的工时不能修改");
        }
        // 非本人且非管理员不允许修改
        if (!SecurityUtils.isAdmin() && !old.getUserId().equals(SecurityUtils.getUserId())) {
            throw new ServiceException("只能修改本人填报的工时");
        }
        checkDailyHours(old.getUserId(), workLog.getWorkDate(), workLog.getHours(), workLog.getLogId());

        // 修改后重新进入待审批
        workLog.setStatus(STATUS_PENDING);
        workLog.setAuditBy(null);
        workLog.setAuditTime(null);
        workLog.setAuditRemark(null);
        workLog.setUpdateTime(LocalDateTime.now());
        return updateById(workLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWorkLogByIds(Long[] logIds) {
        if (logIds == null || logIds.length == 0) {
            return false;
        }
        for (Long logId : logIds) {
            PmWorkLog log = getById(logId);
            if (log == null) {
                continue;
            }
            if (STATUS_PASSED.equals(log.getStatus()) && !SecurityUtils.isAdmin()) {
                throw new ServiceException("已通过的工时不能删除");
            }
            if (!SecurityUtils.isAdmin() && !log.getUserId().equals(SecurityUtils.getUserId())) {
                throw new ServiceException("只能删除本人填报的工时");
            }
        }
        return removeByIds(Arrays.asList(logIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Long[] logIds, String status, String remark) {
        if (logIds == null || logIds.length == 0) {
            return false;
        }
        if (!STATUS_PASSED.equals(status) && !STATUS_REJECTED.equals(status)) {
            throw new ServiceException("审批结果不合法");
        }
        Long auditBy = SecurityUtils.getUserId();
        LocalDateTime now = LocalDateTime.now();
        boolean allOk = true;
        for (Long logId : logIds) {
            PmWorkLog update = new PmWorkLog();
            update.setLogId(logId);
            update.setStatus(status);
            update.setAuditBy(auditBy);
            update.setAuditTime(now);
            update.setAuditRemark(remark);
            update.setUpdateTime(now);
            allOk = updateById(update) && allOk;
        }
        return allOk;
    }

    /**
     * 校验单日填报工时不超过上限
     */
    private void checkDailyHours(Long userId, Object workDate, BigDecimal hours, Long excludeId) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("工时数必须大于 0");
        }
        if (workDate == null) {
            return;
        }
        BigDecimal exists = baseMapper.sumUserDayHours(
                userId, workDate.toString(), excludeId == null ? 0L : excludeId);
        BigDecimal total = (exists == null ? BigDecimal.ZERO : exists).add(hours);
        if (total.compareTo(MAX_DAILY_HOURS) > 0) {
            throw new ServiceException("当日累计工时不能超过 24 小时（当前已填 "
                    + (exists == null ? BigDecimal.ZERO : exists) + " 小时）");
        }
    }
}
