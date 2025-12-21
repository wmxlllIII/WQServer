package com.example.test.server.service.implClass;

import com.example.test.common.enums.EventType;
import com.example.test.common.utils.*;
import com.example.test.pojo.dto.WebSocketDTO;
import com.example.test.pojo.entity.FriendRelationship;
import com.example.test.pojo.entity.Msg;
import com.example.test.pojo.entity.OfflineMsg;
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
import java.util.stream.Collectors;

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
        getPendingMsg(userId);
    }

    private void getPendingFriReq(long userId) {
        List<OfflineMsg> pendingRelas = userMapper.getPendingRelas(userId, EventType.getIntEventType(EventType.EVENT_TYPE_REQUEST_FRIEND));
        log.info("离线好友申请的数量:{}", pendingRelas.size());
        if (pendingRelas.isEmpty()) {
            log.info("[x] getPendingFriReq #84");
            return;
        }

        List<Map<String, Object>> requestList = new ArrayList<>();
        for (OfflineMsg relation : pendingRelas) {
            long senderId = relation.getSenderId();
            User sender = userMapper.getByUuNumber(senderId);
            User receiver = userMapper.getByUuNumber(userId);
            FriendRelationship targetRela = userMapper.getTargetRela(userId, senderId);
            Map<String, Object> request = BuildRelaUtil.buildRequest(sender, receiver, targetRela);
            requestList.add(request);
        }
        pushToUser(userId, EventType.EVENT_TYPE_REQUEST_FRIEND, requestList);
        pendingRelas.forEach(it -> userMapper.deletePendingMsg(it.getId()));
    }

    private void getPendingMsg(long userId) {
        List<OfflineMsg> pendingMsg = userMapper.getPendingMsg(userId, EventType.getIntEventType(EventType.EVENT_TYPE_MSG));
        log.info("离线消息的数量:{}", pendingMsg.size());
        if (pendingMsg.isEmpty()) {
            log.info("[x] getPendingMsg #99");
            return;
        }

        List<MsgVO> msgList = new ArrayList<>();
        for (OfflineMsg offlineMsg : pendingMsg) {
            Msg msg = userMapper.getMsg(offlineMsg.getMsgId());
            MsgVO vo = new MsgVO();
            vo.setMsgId(msg.getId());
            vo.setSenderId(msg.getSenderId());
            vo.setReceiverId(msg.getReceiverId());
            vo.setContent(msg.getContent());
            vo.setType(msg.getType());
            vo.setCreateAt(TimeUtil.dateTimeToSecond(msg.getCreateAt()));
            msgList.add(vo);
        }
        pushToUser(userId, EventType.EVENT_TYPE_MSG, msgList);
        pendingMsg.forEach(it -> userMapper.deletePendingMsg(it.getId()));
    }

}