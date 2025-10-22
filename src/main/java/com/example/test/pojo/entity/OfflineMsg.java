package com.example.test.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineMsg {

    private int messageId;


    private String senderId;


    private String receiverId;

    private int groupId;


    private boolean contentType;

    private long createTime;

    private long expiryTime;

    private boolean isDelivered;

    private boolean isDelete;
}
