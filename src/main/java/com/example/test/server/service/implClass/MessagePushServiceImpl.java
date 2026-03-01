package com.example.test.server.service.implClass;

import com.example.test.common.enums.EventType;
import com.example.test.common.enums.FriendStatuType;
import com.example.test.common.utils.*;
import com.example.test.pojo.dto.WebSocketDTO;
import com.example.test.pojo.entity.ConversationRead;
import com.example.test.pojo.entity.FriendRelationship;
import com.example.test.pojo.entity.Msg;
import com.example.test.pojo.entity.User;
import com.example.test.pojo.vo.MsgVO;
import com.example.test.server.mapper.UserMapper;
import com.example.test.server.service.MessagePushService;
import com.example.test.server.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class MessagePushServiceImpl implements MessagePushService {
    private static final ExecutorService PUSH_THREAD_POOL = Executors.newFixedThreadPool(10);

    @Autowired
    private VerifyUtil verifyUtil;
    @Autowired
    private UserMapper userMapper;

    @Override
    public <T> void pushToUser(long receiver_id, EventType eventType, T data) {
//        // 1. 用 WebSocketDTO 包装消息（核心：统一格式）
        WebSocketDTO<T> wsMsg = new WebSocketDTO<>(eventType.getType(), data);
//        // 2. 异步推送（避免阻塞业务）
        PUSH_THREAD_POOL.submit(() -> doPush(receiver_id, wsMsg));
    }

    @Override
    public <T> void pushToAll(EventType eventType, T data) {

    }


    private <T> void doPush(long receiverId, WebSocketDTO<T> wsMsg) {
        try {
            String jsonMsg = JsonUtil.toJson(wsMsg);
            log.info("准备推送消息：receiverId={}, msg={}", receiverId, jsonMsg);

            if (verifyUtil.getOnlineState(receiverId)) {
                boolean pushSuccess = WebSocketServer.sendMessageToUser(receiverId, jsonMsg);
            }
        } catch (Exception e) {
            log.error("推送消息异常", e);
        }
    }

    @Override
    public void checkPendingRequest(long userId) {
        getPendingFriReq(userId);
        pushUnreadMessages(userId);
    }

    private void getPendingFriReq(long userId) {
        // 从 friend_relationships 表中获取待处理的好友请求
        List<FriendRelationship> pendingRelas = userMapper.getPendingFriendRequests(userId, FriendStatuType.PENDING.getValue());
        log.info("待处理的好友请求数量: {}", pendingRelas.size());

        if (pendingRelas.isEmpty()) {
            log.info("[x] getPendingFriReq #84");
            return;
        }

        // 准备要发送的数据
        List<Map<String, Object>> requestList = new ArrayList<>();
        for (FriendRelationship relation : pendingRelas) {
            long senderId = relation.getSenderId();
            User sender = userMapper.getByUuNumber(senderId);
            User receiver = userMapper.getByUuNumber(userId);
            Map<String, Object> request = BuildRelaUtil.buildRequest(sender, receiver, relation);
            requestList.add(request);
        }

        // 推送待处理的好友请求
        pushToUser(userId, EventType.EVENT_TYPE_REQUEST_FRIEND, requestList);
    }

    private void pushUnreadMessages(long userId) {

        List<ConversationRead> conversations = userMapper.getUserConversations(userId);

        if (conversations == null || conversations.isEmpty()) {
            log.info("[✓] pushUnreadMessages #93");
            return;
        }

        for (ConversationRead cr : conversations) {

            List<Msg> unreadList = userMapper.selectUnreadMessages(
                    cr.getChatType(),
                    cr.getChatId(),
                    cr.getLastMsgId() == 0 ? 0 : cr.getLastMsgId()
            );

            if (unreadList.isEmpty()) {
                continue;
            }

            List<MsgVO> voList = new ArrayList<>();

            for (Msg msg : unreadList) {
                MsgVO vo = new MsgVO();
                vo.setMsgId(msg.getId());
                vo.setSenderId(msg.getSenderId());
                vo.setReceiverId(msg.getChatId());
                vo.setContent(msg.getContent());
                vo.setType(msg.getChatType());
                vo.setCreateAt(TimeUtil.dateTimeToSecond(msg.getCreateAt()));
                voList.add(vo);
            }

            // 推送
            pushToUser(userId, EventType.EVENT_TYPE_MSG, voList);

            // 更新游标为最后一条消息ID
            int latestMsgId = unreadList.get(unreadList.size() - 1).getId();

            userMapper.updateLastMsgId(
                    userId,
                    cr.getChatId(),
                    latestMsgId
            );
        }
    }

}