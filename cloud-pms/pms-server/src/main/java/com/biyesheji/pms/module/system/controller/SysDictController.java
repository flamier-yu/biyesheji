package com.biyesheji.pms.module.system.controller;

import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.system.entity.SysDictData;
import com.biyesheji.pms.module.system.entity.SysDictType;
import com.biyesheji.pms.module.system.service.ISysDictService;
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
 * 字典管理
 */
@Tag(name = "07. 字典管理", description = "字典类型与字典数据维护")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
@Validated
public class SysDictController {

    private final ISysDictService dictService;

    /* ==================== 字典类型 ==================== */

    @Operation(summary = "分页查询字典类型")
    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/type/list")
    public R<PageResult<SysDictType>> typeList(SysDictType dictType,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(dictService.selectDictTypePage(dictType, pageNum, pageSize));
    }

    @Operation(summary = "查询全部字典类型（下拉用）")
    @GetMapping("/type/all")
    public R<List<SysDictType>> typeAll() {
        return R.ok(dictService.selectDictTypeAll());
    }

    @Operation(summary = "查询字典类型详情")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/type/{dictId}")
    public R<SysDictType> typeInfo(@PathVariable Long dictId) {
        return R.ok(dictService.selectDictTypeAll().stream()
                .filter(t -> t.getDictId().equals(dictId))
                .findFirst().orElse(null));
    }

    @Operation(summary = "新增字典类型")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/type")
    public R<Void> addType(@Valid @RequestBody SysDictType dictType) {
        return R.toAjax(dictService.insertDictType(dictType));
    }

    @Operation(summary = "修改字典类型")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PutMapping("/type")
    public R<Void> editType(@Valid @RequestBody SysDictType dictType) {
        return R.toAjax(dictService.updateDictType(dictType));
    }

    @Operation(summary = "删除字典类型（支持批量）")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "字典管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/type/{dictIds}")
    public R<Void> removeType(@PathVariable Long[] dictIds) {
        return R.toAjax(dictService.deleteDictTypeByIds(dictIds));
    }

    /* ==================== 字典数据 ==================== */

    @Operation(summary = "按字典类型查询字典数据")
    @GetMapping("/data/type/{dictType}")
    public R<List<SysDictData>> dataByType(@PathVariable String dictType) {
        return R.ok(dictService.selectDictDataByType(dictType));
    }

    @Operation(summary = "分页查询字典数据")
    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/data/list")
    public R<PageResult<SysDictData>> dataList(SysDictData dictData,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(dictService.selectDictDataPage(dictData, pageNum, pageSize));
    }

    @Operation(summary = "新增字典数据")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/data")
    public R<Void> addData(@Valid @RequestBody SysDictData dictData) {
        return R.toAjax(dictService.insertDictData(dictData));
    }

    @Operation(summary = "修改字典数据")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PutMapping("/data")
    public R<Void> editData(@Valid @RequestBody SysDictData dictData) {
        return R.toAjax(dictService.updateDictData(dictData));
    }

    @Operation(summary = "删除字典数据（支持批量）")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "字典管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/data/{dictCodes}")
    public R<Void> removeData(@PathVariable Long[] dictCodes) {
        return R.toAjax(dictService.deleteDictDataByIds(dictCodes));
    }
}
