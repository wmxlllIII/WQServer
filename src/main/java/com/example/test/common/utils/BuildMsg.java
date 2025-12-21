package com.example.test.common.utils;

import com.example.test.common.enums.ContentType;
import com.example.test.common.enums.EventType;
import com.example.test.pojo.dto.ShareMessageDTO;
import com.example.test.pojo.entity.Msg;
import com.example.test.pojo.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;


public class BuildMsg {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Msg buildSaveMsg(User sender, User receiver, String content) {
        Msg msg = new Msg();

        msg.setSenderId(sender.getUuNumber());
        msg.setType(ContentType.TYPE_TEXT.toInt());
        msg.setReceiverId(receiver.getUuNumber());
        msg.setContent(content);
        return msg;
    }

    public static Msg buildShareMsg(User sender, User receiver, ShareMessageDTO shareMsgDTO) {
        Msg msg = new Msg();
        msg.setType(ContentType.TYPE_LINK.toInt());
        msg.setSenderId(sender.getUuNumber());
        msg.setSenderEmail(sender.getEmail());
        msg.setReceiverEmail(receiver.getEmail());
        msg.setReceiverId(receiver.getUuNumber());
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
