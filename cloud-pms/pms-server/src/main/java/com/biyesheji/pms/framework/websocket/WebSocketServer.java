package com.biyesheji.pms.framework.websocket;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息推送 WebSocket 端点
 * <p>
 * 连接地址：ws://host:8080/ws/message/{userId}
 * 前端登录后按当前用户 ID 建立连接，服务端在产生新消息时主动推送，
 * 避免前端轮询接口。
 */
@Slf4j
@Component
@ServerEndpoint("/ws/message/{userId}")
public class WebSocketServer {

    /** 在线会话：userId -> Session */
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        SESSIONS.put(userId, session);
        log.info("WebSocket 连接建立：userId={}，当前在线 {} 人", userId, SESSIONS.size());
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        SESSIONS.remove(userId);
        log.info("WebSocket 连接关闭：userId={}，当前在线 {} 人", userId, SESSIONS.size());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long userId) {
        // 心跳保活
        if ("ping".equalsIgnoreCase(message)) {
            sendRaw(userId, "pong");
        }
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Long userId) {
        SESSIONS.remove(userId);
        log.warn("WebSocket 异常：userId={}，{}", userId, error.getMessage());
    }

    /**
     * 向指定用户推送消息对象
     */
    public static void sendTo(Long userId, Object data) {
        if (userId == null || data == null) {
            return;
        }
        sendRaw(userId, JSONUtil.toJsonStr(data));
    }

    private static void sendRaw(Long userId, String text) {
        Session session = SESSIONS.get(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.getBasicRemote().sendText(text);
            }
        } catch (IOException e) {
            log.warn("WebSocket 推送失败：userId={}，{}", userId, e.getMessage());
            SESSIONS.remove(userId);
        }
    }

    /**
     * 当前在线人数（可用于监控接口展示）
     */
    public static int onlineCount() {
        return SESSIONS.size();
    }
}
