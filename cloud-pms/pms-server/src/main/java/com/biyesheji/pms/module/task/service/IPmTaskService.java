package com.biyesheji.pms.module.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.module.task.entity.PmTask;
import com.biyesheji.pms.module.task.entity.PmTaskComment;

import java.util.List;

/**
 * 任务服务
 */
public interface IPmTaskService extends IService<PmTask> {

    PageResult<PmTask> selectTaskPage(PmTask task, Integer pageNum, Integer pageSize);

    /**
     * 项目下的任务树（父子任务）
     */
    List<PmTask> selectTaskTreeByProject(Long projectId);

    /**
     * 我负责的任务
     */
    List<PmTask> selectMyTasks();

    /**
     * 看板数据：按状态分组的任务
     */
    List<PmTask> selectBoardData(Long projectId);

    boolean insertTask(PmTask task);

    boolean updateTask(PmTask task);

    boolean deleteTaskByIds(Long[] taskIds);

    /**
     * 任务状态流转（联动项目进度）
     */
    boolean changeStatus(Long taskId, String status, Integer progress);

    /**
     * 指派任务
     */
    boolean assignTask(Long taskId, Long assigneeId);

    /* ========== 评论 ========== */

    List<PmTaskComment> selectComments(Long taskId);

    boolean addComment(PmTaskComment comment);

    boolean deleteComment(Long commentId);
}
