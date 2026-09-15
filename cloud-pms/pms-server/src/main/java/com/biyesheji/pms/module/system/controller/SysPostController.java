package com.biyesheji.pms.module.system.controller;

import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.system.entity.SysPost;
import com.biyesheji.pms.module.system.service.ISysPostService;
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
 * 岗位管理
 */
@Tag(name = "06. 岗位管理", description = "岗位增删改查")
@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
@Validated
public class SysPostController {

    private final ISysPostService postService;

    @Operation(summary = "分页查询岗位列表")
    @PreAuthorize("hasAuthority('system:post:list')")
    @GetMapping("/list")
    public R<PageResult<SysPost>> list(SysPost post,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(postService.selectPostPage(post, pageNum, pageSize));
    }

    @Operation(summary = "查询全部可用岗位（下拉用）")
    @GetMapping("/all")
    public R<List<SysPost>> all() {
        return R.ok(postService.selectPostAll());
    }

    @Operation(summary = "查询岗位详情")
    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/{postId}")
    public R<SysPost> getInfo(@PathVariable Long postId) {
        return R.ok(postService.getById(postId));
    }

    @Operation(summary = "新增岗位")
    @PreAuthorize("hasAuthority('system:post:add')")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysPost post) {
        postService.checkPostCodeUnique(post);
        return R.toAjax(postService.save(post));
    }

    @Operation(summary = "修改岗位")
    @PreAuthorize("hasAuthority('system:post:edit')")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysPost post) {
        postService.checkPostCodeUnique(post);
        return R.toAjax(postService.updateById(post));
    }

    @Operation(summary = "删除岗位（支持批量）")
    @PreAuthorize("hasAuthority('system:post:remove')")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public R<Void> remove(@PathVariable Long[] postIds) {
        for (Long postId : postIds) {
            postService.checkPostCanDelete(postId);
        }
        return R.toAjax(postService.removeByIds(java.util.Arrays.asList(postIds)));
    }
}
