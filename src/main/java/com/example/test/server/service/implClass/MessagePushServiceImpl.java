package com.example.test.server.service.implClass;

import com.example.test.common.context.BaseContext;
import com.example.test.common.exception.BaseException;
import com.example.test.common.utils.BuildMsg;
import com.example.test.common.utils.BuildRelaUtil;
import com.example.test.common.utils.VerifyUtil;
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

@Slf4j
@Service
public class MessagePushServiceImpl implements MessagePushService {

    @Autowired
    private VerifyUtil verifyUtil;


    @Autowired
    private UserMapper userMapper;


    @Override
    public void pushToUser(String receiverId, Map<String, Object> message) {
        boolean onlineState = verifyUtil.getOnlineState(receiverId);
        if (onlineState) {

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonMsg = objectMapper.writeValueAsString(message);
                String currentId = BaseContext.getCurrentId();
                userMapper.requestSended(currentId, receiverId);
                WebSocketServer.sendMessageToUser(receiverId, jsonMsg);
            } catch (Exception e) {
                log.info("错误信息:e{}", e.getMessage());
                throw new BaseException("");
            }
        } else {
            //请求持久化数据库,推送之前已经持久化,状态pending ,推送成功后为sended状态,
            // 不在线则保持pending等再次上线时获取pending状态的请求发送推送
        }

    }

    @Override
    public void notifyPendingRequest(String userId) {
        List<FriendRelationship> pendingRequests = userMapper.getPendingRelas(userId);
        log.info("pendingRequests的数量:{}", pendingRequests.size());
        if (!pendingRequests.isEmpty()) {

            List<Map<String, Object>> requestList = new ArrayList<>();

            for (FriendRelationship relation : pendingRequests) {
                String senderId = relation.getSenderId();
                String receiverId = relation.getReceiverId();
                User sender = userMapper.getById(senderId);
                User receiver = userMapper.getById(receiverId);

                Map<String, Object> request = BuildRelaUtil.buildRequest(sender, receiver, relation);

                requestList.add(request);
            }
            Map<String, Object> message = new HashMap<>();
            message.put("event_type", "FRIEND_REQUEST");
            message.put("request_list", requestList);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonMsg = objectMapper.writeValueAsString(message);
                WebSocketServer.sendMessageToUser(userId, jsonMsg);
                userMapper.requestAllSended(userId);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public void notifyMSg(String userId) {
        List<OfflineMsg> allOfflineMsg = userMapper.getAllOfflineMsg(userId);
        if (allOfflineMsg == null || allOfflineMsg.isEmpty())
            return;
        List<Msg> textMsgList = new ArrayList<>();
        List<Msg> shareMsgList = new ArrayList<>();
        for (OfflineMsg offlineMsg : allOfflineMsg) {
            int messageId = offlineMsg.getMessageId();
            List<Msg> allMsg = userMapper.getAllMsg(messageId);
            for (Msg msg : allMsg) {
                User sender = userMapper.getById(msg.getSenderId());
                User receiver = userMapper.getById(msg.getReceiverId());
                Msg pushMsg = BuildMsg.buildPushMsg(sender, receiver, msg.getContent());
                if (msg.getType()==0){
                    textMsgList.add(pushMsg);
                } else if (msg.getType()==1) {
                    shareMsgList.add(pushMsg);
                }

            }
            userMapper.deleteMsg(messageId);
        }
        if (!textMsgList.isEmpty()) {
            pushTextMSg(userId,"MESSAGE",textMsgList);
        }
        if (!shareMsgList.isEmpty()) {
            pushTextMSg(userId,"SHARE",shareMsgList);
        }


    }
    private void pushTextMSg(String userId, String eventType,List<Msg> messageList) {
        Map<String, Object> message = new HashMap<>();
        message.put("event_type", eventType);
        message.put("msg_list", messageList);
        pushToUser(userId, message);
    }


}
