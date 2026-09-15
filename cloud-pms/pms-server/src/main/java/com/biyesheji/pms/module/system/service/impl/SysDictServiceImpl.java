package com.biyesheji.pms.module.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.module.system.entity.SysDictData;
import com.biyesheji.pms.module.system.entity.SysDictType;
import com.biyesheji.pms.module.system.mapper.SysDictDataMapper;
import com.biyesheji.pms.module.system.mapper.SysDictTypeMapper;
import com.biyesheji.pms.module.system.service.ISysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 字典服务实现
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements ISysDictService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    /* ==================== 字典类型 ==================== */

    @Override
    public PageResult<SysDictType> selectDictTypePage(SysDictType dictType, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (dictType != null) {
            if (StrUtil.isNotBlank(dictType.getDictName())) {
                wrapper.like(SysDictType::getDictName, dictType.getDictName());
            }
            if (StrUtil.isNotBlank(dictType.getDictType())) {
                wrapper.like(SysDictType::getDictType, dictType.getDictType());
            }
            if (StrUtil.isNotBlank(dictType.getStatus())) {
                wrapper.eq(SysDictType::getStatus, dictType.getStatus());
            }
        }
        wrapper.orderByAsc(SysDictType::getDictId);
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        dictTypeMapper.selectPage(page, wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<SysDictType> selectDictTypeAll() {
        return dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getStatus, Constants.STATUS_NORMAL)
                .orderByAsc(SysDictType::getDictId));
    }

    @Override
    public boolean insertDictType(SysDictType dictType) {
        checkDictTypeUnique(dictType);
        return dictTypeMapper.insert(dictType) > 0;
    }

    @Override
    public boolean updateDictType(SysDictType dictType) {
        checkDictTypeUnique(dictType);
        return dictTypeMapper.updateById(dictType) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictTypeByIds(Long[] dictIds) {
        if (dictIds == null || dictIds.length == 0) {
            return false;
        }
        for (Long dictId : dictIds) {
            SysDictType type = dictTypeMapper.selectById(dictId);
            if (type != null) {
                // 同步清理该类型下的字典数据，避免留下孤儿数据
                dictDataMapper.deleteByType(type.getDictType());
            }
        }
        return dictTypeMapper.deleteBatchIds(Arrays.asList(dictIds)) > 0;
    }

    @Override
    public void checkDictTypeUnique(SysDictType dictType) {
        Long dictId = dictType.getDictId() == null ? -1L : dictType.getDictId();
        SysDictType exist = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType.getDictType()));
        if (exist != null && !exist.getDictId().equals(dictId)) {
            throw new ServiceException("字典类型已存在");
        }
    }

    /* ==================== 字典数据 ==================== */

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, Constants.STATUS_NORMAL)
                .orderByAsc(SysDictData::getDictSort));
    }

    @Override
    public PageResult<SysDictData> selectDictDataPage(SysDictData dictData, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        if (dictData != null) {
            if (StrUtil.isNotBlank(dictData.getDictType())) {
                wrapper.eq(SysDictData::getDictType, dictData.getDictType());
            }
            if (StrUtil.isNotBlank(dictData.getDictLabel())) {
                wrapper.like(SysDictData::getDictLabel, dictData.getDictLabel());
            }
            if (StrUtil.isNotBlank(dictData.getStatus())) {
                wrapper.eq(SysDictData::getStatus, dictData.getStatus());
            }
        }
        wrapper.orderByAsc(SysDictData::getDictType).orderByAsc(SysDictData::getDictSort);
        Page<SysDictData> page = new Page<>(pageNum, pageSize);
        dictDataMapper.selectPage(page, wrapper);
        return PageResult.of(page);
    }

    @Override
    public boolean insertDictData(SysDictData dictData) {
        return dictDataMapper.insert(dictData) > 0;
    }

    @Override
    public boolean updateDictData(SysDictData dictData) {
        return dictDataMapper.updateById(dictData) > 0;
    }

    @Override
    public boolean deleteDictDataByIds(Long[] dictCodes) {
        if (dictCodes == null || dictCodes.length == 0) {
            return false;
        }
        return dictDataMapper.deleteBatchIds(Arrays.asList(dictCodes)) > 0;
    }
}
