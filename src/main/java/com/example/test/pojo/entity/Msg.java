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
public class Msg {

    private int id;

    private long senderId;

    private long chatId;

    private int chatType;

    private int messageType;

    private String content;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;


    public static final int CHAT_INDIVIDUAL = 100;    // 私聊 = 100;
    public static final int CHAT_GROUP = 101;    // 群聊 = 101;


    public static final int TYPE_TEXT = 0;    // 文字
    public static final int TYPE_IMAGE = 1;   // 图片
    public static final int TYPE_VOICE = 2;   // 语音
    public static final int TYPE_VIDEO = 3;   // 视频
    public static final int TYPE_FILE = 4;    // 其他文件（如文档）
}
