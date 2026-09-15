package com.biyesheji.pms.module.system.service;

import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.module.system.entity.SysDictData;
import com.biyesheji.pms.module.system.entity.SysDictType;

import java.util.List;

/**
 * 字典服务（统一管理字典类型与字典数据）
 */
public interface ISysDictService {

    /* ========== 字典类型 ========== */

    PageResult<SysDictType> selectDictTypePage(SysDictType dictType, Integer pageNum, Integer pageSize);

    List<SysDictType> selectDictTypeAll();

    boolean insertDictType(SysDictType dictType);

    boolean updateDictType(SysDictType dictType);

    boolean deleteDictTypeByIds(Long[] dictIds);

    void checkDictTypeUnique(SysDictType dictType);

    /* ========== 字典数据 ========== */

    /**
     * 按字典类型查询字典数据（前端下拉/标签回显用）
     */
    List<SysDictData> selectDictDataByType(String dictType);

    PageResult<SysDictData> selectDictDataPage(SysDictData dictData, Integer pageNum, Integer pageSize);

    boolean insertDictData(SysDictData dictData);

    boolean updateDictData(SysDictData dictData);

    boolean deleteDictDataByIds(Long[] dictCodes);
}
