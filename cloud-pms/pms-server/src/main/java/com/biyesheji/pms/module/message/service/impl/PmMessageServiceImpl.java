package com.biyesheji.pms.module.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.framework.security.SecurityUtils;
import com.biyesheji.pms.framework.websocket.WebSocketServer;
import com.biyesheji.pms.module.message.entity.PmMessage;
import com.biyesheji.pms.module.message.mapper.PmMessageMapper;
import com.biyesheji.pms.module.message.service.IPmMessageService;
import com.biyesheji.pms.module.system.entity.SysUser;
import com.biyesheji.pms.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PmMessageServiceImpl extends ServiceImpl<PmMessageMapper, PmMessage>
        implements IPmMessageService {

    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<PmMessage> selectMessagePage(PmMessage message, Integer pageNum, Integer pageSize) {
        Long userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<PmMessage> wrapper = new LambdaQueryWrapper<PmMessage>()
                .eq(PmMessage::getReceiverId, userId);
        if (message != null) {
            if (StrUtil.isNotBlank(message.getMsgType())) {
                wrapper.eq(PmMessage::getMsgType, message.getMsgType());
            }
            if (StrUtil.isNotBlank(message.getIsRead())) {
                wrapper.eq(PmMessage::getIsRead, message.getIsRead());
            }
            if (StrUtil.isNotBlank(message.getTitle())) {
                wrapper.like(PmMessage::getTitle, message.getTitle());
            }
        }
        wrapper.orderByDesc(PmMessage::getMessageId);
        Page<PmMessage> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillSenderName(page.getRecords());
        return PageResult.of(page);
    }

    private void fillSenderName(List<PmMessage> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<Long> senderIds = list.stream().map(PmMessage::getSenderId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> userMap = new HashMap<>();
        if (!senderIds.isEmpty()) {
            userMap = sysUserMapper.selectBatchIds(senderIds).stream()
                    .collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName, (a, b) -> a));
        }
        for (PmMessage m : list) {
            m.setSenderName(m.getSenderId() == null ? "系统" : userMap.get(m.getSenderId()));
        }
    }

    @Override
    public int countUnread() {
        return baseMapper.countUnread(SecurityUtils.getUserId());
    }

    @Override
    public List<Map<String, Object>> countUnreadByType() {
        return baseMapper.countUnreadByType(SecurityUtils.getUserId());
    }

    @Override
    public boolean readByIds(Long[] messageIds) {
        if (messageIds == null || messageIds.length == 0) {
            return false;
        }
        return baseMapper.readByIds(SecurityUtils.getUserId(), Arrays.asList(messageIds)) >= 0;
    }

    @Override
    public boolean readAll() {
        baseMapper.readAll(SecurityUtils.getUserId());
        return true;
    }

    @Override
    public boolean deleteByIds(Long[] messageIds) {
        if (messageIds == null || messageIds.length == 0) {
            return false;
        }
        Long userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<PmMessage> wrapper = new LambdaQueryWrapper<PmMessage>()
                .in(PmMessage::getMessageId, Arrays.asList(messageIds))
                .eq(PmMessage::getReceiverId, userId);
        return remove(wrapper);
    }

    /* ==================== 发送 ==================== */

    @Override
    public void send(Long receiverId, String title, String content,
                     String msgType, String bizType, Long bizId) {
        if (receiverId == null) {
            return;
        }
        PmMessage message = buildMessage(receiverId, title, content, msgType, bizType, bizId);
        save(message);
        // 实时推送（用户不在线时静默忽略，下次进消息中心仍能看到）
        WebSocketServer.sendTo(receiverId, message);
    }

    @Override
    public void sendBatch(List<Long> receiverIds, String title, String content,
                          String msgType, String bizType, Long bizId) {
        if (CollUtil.isEmpty(receiverIds)) {
            return;
        }
        List<PmMessage> messages = new ArrayList<>();
        for (Long receiverId : receiverIds.stream().filter(Objects::nonNull).distinct()
                .collect(Collectors.toList())) {
            PmMessage message = buildMessage(receiverId, title, content, msgType, bizType, bizId);
            message.setSenderId(null);
            messages.add(message);
        }
        saveBatch(messages);
        messages.forEach(m -> WebSocketServer.sendTo(m.getReceiverId(), m));
    }

    private PmMessage buildMessage(Long receiverId, String title, String content,
                                   String msgType, String bizType, Long bizId) {
        PmMessage message = new PmMessage();
        message.setReceiverId(receiverId);
        message.setSenderId(SecurityUtils.getUserIdOrNull());
        message.setTitle(title);
        message.setContent(content);
        message.setMsgType(StrUtil.isBlank(msgType) ? "0" : msgType);
        message.setBizType(bizType);
        message.setBizId(bizId);
        message.setIsRead("0");
        message.setCreateTime(LocalDateTime.now());
        return message;
    }
}
