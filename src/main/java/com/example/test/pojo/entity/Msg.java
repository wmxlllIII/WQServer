package com.example.test.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Msg {

    private int id;

    private String sessionId;

    private String senderId;

    private String senderEmail;

    private String receiverEmail;

    private String receiverId;

    private int type;//0文本   1分享消息  2

    private String content;

    private long timestamp;


}
