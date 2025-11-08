package com.example.test.server.service.implClass;

import com.example.test.common.context.BaseContext;
import com.example.test.common.enums.EventType;
import com.example.test.common.exception.BaseException;
import com.example.test.common.utils.BuildMsg;
import com.example.test.common.utils.BuildRelaUtil;
import com.example.test.common.utils.VerifyUtil;
import com.example.test.pojo.dto.MsgDTO;
import com.example.test.pojo.dto.WebSocketDTO;
import com.example.test.pojo.entity.FriendRelationship;
import com.example.test.pojo.entity.Msg;
import com.example.test.pojo.entity.OfflineMsg;
import com.example.test.pojo.entity.User;
import com.example.test.server.mapper.UserMapper;
import com.example.test.server.service.MessagePushService;
import com.example.test.server.websocket.WebSocketServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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
    @Autowired
    private OfflineMsgMapper offlineMsgMapper;


    @Override
    public <T> void pushToUser(String receiverId, EventType eventType, T data) {
        // 1. 用 WebSocketDTO 包装消息（核心：统一格式）
        WebSocketDTO<T> wsMsg = new WebSocketDTO<>(eventType.getType(), data);
        // 2. 异步推送（避免阻塞业务）
        PUSH_THREAD_POOL.submit(() -> doPush(receiverId, wsMsg));
    }

    @Override
    public <T> void pushToAll(EventType eventType, T data) {

    }

    private <T> void saveOfflineMsg(String receiverId, WebSocketDTO<T> wsMsg) {
        try {
            MsgDTO chatMsg = (MsgDTO) wsMsg.getData();

            OfflineMsg offlineMsg = new OfflineMsg();
            offlineMsg.setReceiverId(receiverId); // 接收者ID
            offlineMsg.setMsgType(chatMsg.getMsgType()); // 事件类型（方便筛选）
            offlineMsg.setMsgContent(JsonUtil.toJson(wsMsg)); // 存储完整的WebSocketDTO JSON
            offlineMsg.setIsRead(false); // 未读标识
            offlineMsg.setCreateTime(System.currentTimeMillis()); // 创建时间

            offlineMsgMapper.insert(offlineMsg); // 你的离线消息Mapper
        } catch (Exception e) {
            log.error("存储离线消息失败", e);
        }
    }

    private <T> void doPush(String receiverId, WebSocketDTO<T> wsMsg) {
        try {
            String jsonMsg = JsonUtil.toJson(wsMsg);
            log.info("准备推送消息：receiverId={}, msg={}", receiverId, jsonMsg);

            // 3. 判断用户是否在线（用你原有逻辑）
            if (verifyUtil.getOnlineState(receiverId)) {
                // 在线：通过WebSocket推送
                boolean pushSuccess = WebSocketServer.sendMessageToUser(receiverId, jsonMsg);
                if (pushSuccess) {
                    log.info("推送成功：receiverId={}", receiverId);
                } else {
                    log.error("推送失败，转为离线存储：receiverId={}", receiverId);
                    saveOfflineMsg(receiverId, wsMsg);
                }
            } else {
                // 离线：存入离线消息表
                log.warn("用户不在线，存储离线消息：receiverId={}", receiverId);
                saveOfflineMsg(receiverId, wsMsg);
            }
        } catch (Exception e) {
            log.error("推送消息异常", e);
            // 异常兜底：存入离线消息
            saveOfflineMsg(receiverId, wsMsg);
        }
    }

//    @Override
//    public void notifyPendingRequest(String userId) {
//        List<FriendRelationship> pendingRequests = userMapper.getPendingRelas(userId);
//        log.info("pendingRequests的数量:{}", pendingRequests.size());
//        if (!pendingRequests.isEmpty()) {
//
//            List<Map<String, Object>> requestList = new ArrayList<>();
//
//            for (FriendRelationship relation : pendingRequests) {
//                String senderId = relation.getSenderId();
//                String receiverId = relation.getReceiverId();
//                User sender = userMapper.getById(senderId);
//                User receiver = userMapper.getById(receiverId);
//
//                Map<String, Object> request = BuildRelaUtil.buildRequest(sender, receiver, relation);
//
//                requestList.add(request);
//            }
//            Map<String, Object> message = new HashMap<>();
//            message.put("event_type", "FRIEND_REQUEST");
//            message.put("request_list", requestList);
//
//            try {
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonMsg = objectMapper.writeValueAsString(message);
//                WebSocketServer.sendMessageToUser(userId, jsonMsg);
//                userMapper.requestAllSended(userId);
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
//    }
//
//    @Override
//    public void notifyMSg(String userId) {
//        List<OfflineMsg> allOfflineMsg = userMapper.getAllOfflineMsg(userId);
//        if (allOfflineMsg == null || allOfflineMsg.isEmpty())
//            return;
//        List<Msg> textMsgList = new ArrayList<>();
//        List<Msg> shareMsgList = new ArrayList<>();
//        for (OfflineMsg offlineMsg : allOfflineMsg) {
//            int messageId = offlineMsg.getMessageId();
//            List<Msg> allMsg = userMapper.getAllMsg(messageId);
//            for (Msg msg : allMsg) {
//                User sender = userMapper.getById(msg.getSenderId());
//                User receiver = userMapper.getById(msg.getReceiverId());
//                Msg pushMsg = BuildMsg.buildPushMsg(sender, receiver, msg.getContent());
//                if (msg.getType() == 0) {
//                    textMsgList.add(pushMsg);
//                } else if (msg.getType() == 1) {
//                    shareMsgList.add(pushMsg);
//                }
//
//            }
//            userMapper.deleteMsg(messageId);
//        }
//        if (!textMsgList.isEmpty()) {
//            pushTextMSg(userId, "MESSAGE", textMsgList);
//        }
//        if (!shareMsgList.isEmpty()) {
//            pushTextMSg(userId, "SHARE", shareMsgList);
//        }
//
//
//    }
//
//    private void pushTextMSg(String userId, String eventType, List<Msg> messageList) {
//        Map<String, Object> message = new HashMap<>();
//        message.put("event_type", eventType);
//        message.put("msg_list", messageList);
//        pushToUser(userId, message);
//    }


}
