package com.example.test.pojo.entity;

import com.example.test.common.enums.ContentType;
import com.example.test.common.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Msg {

    private int id;

    private long senderId;

    private String senderEmail;

    private String receiverEmail;

    private long receiverId;

    private int type;

    private String content;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;
}
