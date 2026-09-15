package com.biyesheji.pms.module.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.file.entity.PmAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 附件 Mapper
 */
@Mapper
public interface PmAttachmentMapper extends BaseMapper<PmAttachment> {

    @Select("select a.*, u.nick_name as upload_name from pm_attachment a " +
            "left join sys_user u on u.user_id = a.upload_by " +
            "where a.del_flag = '0' and a.biz_type = #{bizType} and a.biz_id = #{bizId} " +
            "order by a.attach_id desc")
    List<PmAttachment> selectByBiz(@Param("bizType") String bizType, @Param("bizId") Long bizId);

    /**
     * 按 MD5 查找已存在的文件（用于秒传）
     */
    @Select("select * from pm_attachment where del_flag = '0' and file_md5 = #{md5} limit 1")
    PmAttachment selectByMd5(@Param("md5") String md5);
}
