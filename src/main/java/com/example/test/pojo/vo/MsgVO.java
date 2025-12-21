package com.example.test.pojo.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MsgVO {
    private long ChatId;
    private int MsgId;
    private long senderId;
    private long receiverId;
    private String content;
    private int type;
    private long createAt;
    private long updateAt;
}
