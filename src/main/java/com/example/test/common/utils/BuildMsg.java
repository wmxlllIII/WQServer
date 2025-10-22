package com.example.test.common.utils;

import com.example.test.common.context.BaseContext;
import com.example.test.pojo.dto.ShareMessageDTO;
import com.example.test.pojo.entity.Msg;
import com.example.test.pojo.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;


public class BuildMsg {
    public static final int MSG_TEXT = 0;
    public static final int MSG_SHARE = 1;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Msg setOriginMsg(String senderId, String receiverId, String content) {
        Msg msg = new Msg();
        msg.setSessionId(senderId + "_" + receiverId);
        msg.setType(BaseContext.getCurrentId() == senderId ? 1 : 0);
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        return msg;
    }

    public static Msg buildPushMsg(User sender, User receiver, String content) {
        Msg msg = new Msg();
        msg.setSessionId(sender.getEmail() + "_" + receiver.getEmail());
        msg.setSenderEmail(sender.getEmail());
        msg.setReceiverEmail(receiver.getEmail());
        msg.setContent(content);
        return msg;
    }

    public static Msg buildSaveMsg(User sender, User receiver, String content) {
        Msg msg = new Msg();
        msg.setSessionId(sender.getUuid() + "_" + receiver.getUuid());
        msg.setSenderId(sender.getUuid());
        msg.setType(MSG_TEXT);
        msg.setReceiverId(receiver.getUuid());
        msg.setContent(content);
        return msg;
    }

    public static Msg buildShareMsg(User sender, User receiver, ShareMessageDTO shareMsgDTO) {
        Msg msg = new Msg();
        msg.setType(MSG_SHARE);
        msg.setSessionId(sender.getUuid() + "_" + receiver.getUuid());
        msg.setSenderId(sender.getUuid());
        msg.setSenderEmail(sender.getEmail());
        msg.setReceiverEmail(receiver.getEmail());
        msg.setReceiverId(receiver.getUuid());
        msg.setType(1);
        try {
            Map<String, Object> shareContent = new HashMap<>();
            shareContent.put("linkTitle", shareMsgDTO.getLinkTitle());
            shareContent.put("linkContent", shareMsgDTO.getLinkContent());
            shareContent.put("linkImageUrl", shareMsgDTO.getLinkImageUrl());

            msg.setContent(objectMapper.writeValueAsString(shareContent));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return msg;
    }



}
