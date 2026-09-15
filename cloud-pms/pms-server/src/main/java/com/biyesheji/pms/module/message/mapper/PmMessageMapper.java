package com.biyesheji.pms.module.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biyesheji.pms.module.message.entity.PmMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 消息 Mapper
 */
@Mapper
public interface PmMessageMapper extends BaseMapper<PmMessage> {

    /**
     * 未读消息数量
     */
    @Select("select count(1) from pm_message where del_flag = '0' " +
            "and receiver_id = #{userId} and is_read = '0'")
    int countUnread(@Param("userId") Long userId);

    /**
     * 全部标记为已读
     */
    @Update("update pm_message set is_read = '1', read_time = now() " +
            "where receiver_id = #{userId} and is_read = '0'")
    int readAll(@Param("userId") Long userId);

    /**
     * 批量标记为已读
     */
    @Update("<script>update pm_message set is_read = '1', read_time = now() " +
            "where receiver_id = #{userId} and message_id in " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int readByIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    /**
     * 未读消息按类型统计（消息中心分类角标）
     */
    @Select("select msg_type as msgType, count(1) as num from pm_message " +
            "where del_flag = '0' and receiver_id = #{userId} and is_read = '0' " +
            "group by msg_type")
    List<java.util.Map<String, Object>> countUnreadByType(@Param("userId") Long userId);
}
