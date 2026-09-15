package com.biyesheji.pms.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 更新最后登录信息
     */
    @Update("update sys_user set login_ip = #{loginIp}, login_date = now() where user_id = #{userId}")
    int updateLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);

    /**
     * 查询部门下的用户数量
     */
    @Select("select count(1) from sys_user where dept_id = #{deptId} and del_flag = '0'")
    int countByDeptId(@Param("deptId") Long deptId);
}
