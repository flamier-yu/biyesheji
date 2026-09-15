package com.biyesheji.pms.module.task.controller;

import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.task.entity.PmTask;
import com.biyesheji.pms.module.task.entity.PmTaskComment;
import com.biyesheji.pms.module.task.service.IPmTaskService;
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
 * 任务管理
 */
@Tag(name = "12. 任务管理", description = "任务拆解、指派、看板、甘特图与评论")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@Validated
public class PmTaskController {

    private final IPmTaskService taskService;

    @Operation(summary = "分页查询任务列表")
    @PreAuthorize("hasAuthority('project:task:list')")
    @GetMapping("/list")
    public R<PageResult<PmTask>> list(PmTask task,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(taskService.selectTaskPage(task, pageNum, pageSize));
    }

    @Operation(summary = "查询我负责的任务")
    @GetMapping("/my")
    public R<List<PmTask>> my() {
        return R.ok(taskService.selectMyTasks());
    }

    @Operation(summary = "查询项目的任务树（甘特图/列表用）")
    @PreAuthorize("hasAuthority('project:task:list')")
    @GetMapping("/tree/{projectId}")
    public R<List<PmTask>> tree(@PathVariable Long projectId) {
        return R.ok(taskService.selectTaskTreeByProject(projectId));
    }

    @Operation(summary = "查询项目看板数据（按状态分组）")
    @PreAuthorize("hasAuthority('project:task:list')")
    @GetMapping("/board/{projectId}")
    public R<List<PmTask>> board(@PathVariable Long projectId) {
        return R.ok(taskService.selectBoardData(projectId));
    }

    @Operation(summary = "查询任务详情")
    @PreAuthorize("hasAuthority('project:task:query')")
    @GetMapping("/{taskId}")
    public R<PmTask> getInfo(@PathVariable Long taskId) {
        PmTask task = taskService.getById(taskId);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        return R.ok(task);
    }

    @Operation(summary = "新增任务")
    @PreAuthorize("hasAuthority('project:task:add')")
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody PmTask task) {
        return R.toAjax(taskService.insertTask(task));
    }

    @Operation(summary = "修改任务")
    @PreAuthorize("hasAuthority('project:task:edit')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody PmTask task) {
        return R.toAjax(taskService.updateTask(task));
    }

    @Operation(summary = "删除任务（支持批量）")
    @PreAuthorize("hasAuthority('project:task:remove')")
    @Log(title = "任务管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public R<Void> remove(@PathVariable Long[] taskIds) {
        return R.toAjax(taskService.deleteTaskByIds(taskIds));
    }

    @Operation(summary = "变更任务状态")
    @PreAuthorize("hasAuthority('project:task:edit')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody PmTask task) {
        return R.toAjax(taskService.changeStatus(task.getTaskId(), task.getStatus(), task.getProgress()));
    }

    @Operation(summary = "指派任务负责人")
    @PreAuthorize("hasAuthority('project:task:edit')")
    @Log(title = "任务管理", businessType = BusinessType.GRANT)
    @PutMapping("/assign")
    public R<Void> assign(@RequestBody PmTask task) {
        return R.toAjax(taskService.assignTask(task.getTaskId(), task.getAssigneeId()));
    }

    /* ==================== 评论 ==================== */

    @Operation(summary = "查询任务评论")
    @GetMapping("/{taskId}/comments")
    public R<List<PmTaskComment>> comments(@PathVariable Long taskId) {
        return R.ok(taskService.selectComments(taskId));
    }

    @Operation(summary = "发表评论")
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @PostMapping("/comment")
    public R<Void> addComment(@Valid @RequestBody PmTaskComment comment) {
        return R.toAjax(taskService.addComment(comment));
    }

    @Operation(summary = "删除评论")
    @Log(title = "任务管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/comment/{commentId}")
    public R<Void> removeComment(@PathVariable Long commentId) {
        return R.toAjax(taskService.deleteComment(commentId));
    }
}
