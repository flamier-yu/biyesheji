package com.biyesheji.pms.module.worklog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.module.worklog.entity.PmWorkLog;

import java.util.List;

/**
 * 工时服务
 */
public interface IPmWorkLogService extends IService<PmWorkLog> {

    PageResult<PmWorkLog> selectWorkLogPage(PmWorkLog log, Integer pageNum, Integer pageSize);

    /**
     * 查询我的工时（按日期区间）
     */
    List<PmWorkLog> selectMyLogs(String startDate, String endDate);

    boolean insertWorkLog(PmWorkLog workLog);

    boolean updateWorkLog(PmWorkLog workLog);

    boolean deleteWorkLogByIds(Long[] logIds);

    /**
     * 审批工时（通过 / 驳回）
     */
    boolean audit(Long[] logIds, String status, String remark);
}
