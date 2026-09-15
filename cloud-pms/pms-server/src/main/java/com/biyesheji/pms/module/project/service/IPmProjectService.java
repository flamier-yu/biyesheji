package com.biyesheji.pms.module.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.module.project.entity.PmMilestone;
import com.biyesheji.pms.module.project.entity.PmProject;

import java.util.List;

/**
 * 项目服务
 */
public interface IPmProjectService extends IService<PmProject> {

    PageResult<PmProject> selectProjectPage(PmProject project, Integer pageNum, Integer pageSize);

    /**
     * 项目详情（含成员、里程碑、任务统计）
     */
    PmProject selectProjectDetail(Long projectId);

    boolean insertProject(PmProject project);

    boolean updateProject(PmProject project);

    boolean deleteProjectByIds(Long[] projectIds);

    boolean changeStatus(Long projectId, String status);

    /**
     * 依据任务完成情况重算项目进度
     */
    void recalcProgress(Long projectId);

    /**
     * 当前用户可参与的项目（用于任务、工时下拉）
     */
    List<PmProject> selectMyProjects();

    /* ========== 里程碑 ========== */

    List<PmMilestone> selectMilestones(Long projectId);

    boolean saveMilestone(PmMilestone milestone);

    boolean deleteMilestone(Long milestoneId);
}
