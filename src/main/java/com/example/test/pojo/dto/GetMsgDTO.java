package com.example.test.pojo.dto;

import lombok.Data;

@Data
public class GetMsgDTO {
    private int page;
    private int size;
    private long chatId;
}
