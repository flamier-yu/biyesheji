package com.biyesheji.pms.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.system.entity.SysPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 岗位 Mapper
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {

    @Select("select count(1) from sys_user_post where post_id = #{postId}")
    int countUserByPostId(@Param("postId") Long postId);
}
