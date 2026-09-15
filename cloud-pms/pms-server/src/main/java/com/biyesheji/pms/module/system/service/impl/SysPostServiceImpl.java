package com.biyesheji.pms.module.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biyesheji.pms.common.constant.Constants;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.module.system.entity.SysPost;
import com.biyesheji.pms.module.system.mapper.SysPostMapper;
import com.biyesheji.pms.module.system.service.ISysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位服务实现
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {

    @Override
    public PageResult<SysPost> selectPostPage(SysPost post, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        if (post != null) {
            if (StrUtil.isNotBlank(post.getPostCode())) {
                wrapper.like(SysPost::getPostCode, post.getPostCode());
            }
            if (StrUtil.isNotBlank(post.getPostName())) {
                wrapper.like(SysPost::getPostName, post.getPostName());
            }
            if (StrUtil.isNotBlank(post.getStatus())) {
                wrapper.eq(SysPost::getStatus, post.getStatus());
            }
        }
        wrapper.orderByAsc(SysPost::getOrderNum);
        Page<SysPost> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<SysPost> selectPostAll() {
        return lambdaQuery()
                .eq(SysPost::getStatus, Constants.STATUS_NORMAL)
                .orderByAsc(SysPost::getOrderNum)
                .list();
    }

    @Override
    public void checkPostCodeUnique(SysPost post) {
        Long postId = post.getPostId() == null ? -1L : post.getPostId();
        SysPost exist = lambdaQuery().eq(SysPost::getPostCode, post.getPostCode()).one();
        if (exist != null && !exist.getPostId().equals(postId)) {
            throw new ServiceException("岗位编码已存在");
        }
    }

    @Override
    public void checkPostCanDelete(Long postId) {
        if (baseMapper.countUserByPostId(postId) > 0) {
            throw new ServiceException("岗位已分配给用户，不允许删除");
        }
    }
}
