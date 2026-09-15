package com.biyesheji.pms.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.module.system.entity.SysPost;

import java.util.List;

/**
 * 岗位服务
 */
public interface ISysPostService extends IService<SysPost> {

    PageResult<SysPost> selectPostPage(SysPost post, Integer pageNum, Integer pageSize);

    /**
     * 查询全部可用岗位（下拉用）
     */
    List<SysPost> selectPostAll();

    void checkPostCodeUnique(SysPost post);

    void checkPostCanDelete(Long postId);
}
