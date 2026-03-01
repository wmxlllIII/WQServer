package com.example.test.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRead {

    private int id;

    private long userId;

    private int chatType;

    private long chatId;

    private int lastMsgId;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

}
