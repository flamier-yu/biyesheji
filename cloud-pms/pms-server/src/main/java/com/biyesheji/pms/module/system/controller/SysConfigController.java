package com.biyesheji.pms.module.system.controller;

import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.system.entity.SysConfig;
import com.biyesheji.pms.module.system.service.ISysConfigService;
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
import java.util.Arrays;

/**
 * 参数配置管理
 */
@Tag(name = "08. 参数配置", description = "系统参数维护与缓存刷新")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
@Validated
public class SysConfigController {

    private final ISysConfigService configService;

    @Operation(summary = "分页查询参数列表")
    @PreAuthorize("hasAuthority('system:config:list')")
    @GetMapping("/list")
    public R<PageResult<SysConfig>> list(SysConfig config,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(configService.selectConfigPage(config, pageNum, pageSize));
    }

    @Operation(summary = "按键名查询参数值")
    @GetMapping("/configKey/{configKey}")
    public R<String> getByKey(@PathVariable String configKey) {
        return R.ok(configService.selectValueByKey(configKey));
    }

    @Operation(summary = "查询参数详情")
    @PreAuthorize("hasAuthority('system:config:query')")
    @GetMapping("/{configId}")
    public R<SysConfig> getInfo(@PathVariable Long configId) {
        return R.ok(configService.getById(configId));
    }

    @Operation(summary = "新增参数")
    @PreAuthorize("hasAuthority('system:config:add')")
    @Log(title = "参数配置", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysConfig config) {
        configService.checkConfigKeyUnique(config);
        boolean ok = configService.save(config);
        configService.refreshCache();
        return R.toAjax(ok);
    }

    @Operation(summary = "修改参数")
    @PreAuthorize("hasAuthority('system:config:edit')")
    @Log(title = "参数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysConfig config) {
        configService.checkConfigKeyUnique(config);
        boolean ok = configService.updateById(config);
        configService.refreshCache();
        return R.toAjax(ok);
    }

    @Operation(summary = "删除参数（支持批量）")
    @PreAuthorize("hasAuthority('system:config:remove')")
    @Log(title = "参数配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public R<Void> remove(@PathVariable Long[] configIds) {
        boolean ok = configService.removeByIds(Arrays.asList(configIds));
        configService.refreshCache();
        return R.toAjax(ok);
    }

    @Operation(summary = "刷新参数缓存")
    @PreAuthorize("hasAuthority('system:config:edit')")
    @Log(title = "参数配置", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public R<Void> refreshCache() {
        configService.refreshCache();
        return R.ok();
    }
}
