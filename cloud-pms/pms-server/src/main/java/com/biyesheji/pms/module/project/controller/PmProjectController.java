package com.biyesheji.pms.module.project.controller;

import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.project.entity.PmMilestone;
import com.biyesheji.pms.module.project.entity.PmProject;
import com.biyesheji.pms.module.project.service.IPmProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 项目管理
 */
@Tag(name = "11. 项目管理", description = "项目立项、成员、里程碑与进度管理")
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@Validated
public class PmProjectController {

    private final IPmProjectService projectService;

    @Operation(summary = "分页查询项目列表")
    @PreAuthorize("hasAuthority('project:project:list')")
    @GetMapping("/list")
    public R<PageResult<PmProject>> list(PmProject project,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(projectService.selectProjectPage(project, pageNum, pageSize));
    }

    @Operation(summary = "查询我参与的项目（下拉用）")
    @GetMapping("/my")
    public R<List<PmProject>> my() {
        return R.ok(projectService.selectMyProjects());
    }

    @Operation(summary = "查询项目详情（含成员与里程碑）")
    @PreAuthorize("hasAuthority('project:project:query')")
    @GetMapping("/{projectId}")
    public R<PmProject> getInfo(@PathVariable Long projectId) {
        return R.ok(projectService.selectProjectDetail(projectId));
    }

    @Operation(summary = "新增项目")
    @PreAuthorize("hasAuthority('project:project:add')")
    @Log(title = "项目管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody PmProject project) {
        return R.toAjax(projectService.insertProject(project));
    }

    @Operation(summary = "修改项目")
    @PreAuthorize("hasAuthority('project:project:edit')")
    @Log(title = "项目管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody PmProject project) {
        return R.toAjax(projectService.updateProject(project));
    }

    @Operation(summary = "删除项目（支持批量）")
    @PreAuthorize("hasAuthority('project:project:remove')")
    @Log(title = "项目管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{projectIds}")
    public R<Void> remove(@PathVariable Long[] projectIds) {
        return R.toAjax(projectService.deleteProjectByIds(projectIds));
    }

    @Operation(summary = "变更项目状态")
    @PreAuthorize("hasAuthority('project:project:edit')")
    @Log(title = "项目管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody PmProject project) {
        return R.toAjax(projectService.changeStatus(project.getProjectId(), project.getStatus()));
    }

    @Operation(summary = "重算项目进度")
    @PreAuthorize("hasAuthority('project:project:edit')")
    @PutMapping("/{projectId}/recalc")
    public R<Void> recalc(@PathVariable Long projectId) {
        projectService.recalcProgress(projectId);
        return R.ok();
    }

    /* ==================== 里程碑 ==================== */

    @Operation(summary = "查询项目里程碑")
    @GetMapping("/{projectId}/milestones")
    public R<List<PmMilestone>> milestones(@PathVariable Long projectId) {
        return R.ok(projectService.selectMilestones(projectId));
    }

    @Operation(summary = "新增/修改里程碑")
    @PreAuthorize("hasAuthority('project:milestone:edit')")
    @Log(title = "里程碑管理", businessType = BusinessType.UPDATE)
    @PostMapping("/milestone")
    public R<Void> saveMilestone(@Valid @RequestBody PmMilestone milestone) {
        return R.toAjax(projectService.saveMilestone(milestone));
    }

    @Operation(summary = "删除里程碑")
    @PreAuthorize("hasAuthority('project:milestone:remove')")
    @Log(title = "里程碑管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/milestone/{milestoneId}")
    public R<Void> removeMilestone(@PathVariable Long milestoneId) {
        return R.toAjax(projectService.deleteMilestone(milestoneId));
    }
}
