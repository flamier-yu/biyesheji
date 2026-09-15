package com.biyesheji.pms.module.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.module.message.entity.PmMessage;

import java.util.List;
import java.util.Map;

/**
 * 消息服务
 */
public interface IPmMessageService extends IService<PmMessage> {

    PageResult<PmMessage> selectMessagePage(PmMessage message, Integer pageNum, Integer pageSize);

    /**
     * 当前用户未读消息数
     */
    int countUnread();

    /**
     * 未读消息按类型统计
     */
    List<Map<String, Object>> countUnreadByType();

    boolean readByIds(Long[] messageIds);

    boolean readAll();

    boolean deleteByIds(Long[] messageIds);

    /* ========== 发送（供其它业务模块调用） ========== */

    /**
     * 发送单条站内消息，并尝试 WebSocket 实时推送
     */
    void send(Long receiverId, String title, String content,
              String msgType, String bizType, Long bizId);

    /**
     * 批量发送
     */
    void sendBatch(List<Long> receiverIds, String title, String content,
                   String msgType, String bizType, Long bizId);
}
