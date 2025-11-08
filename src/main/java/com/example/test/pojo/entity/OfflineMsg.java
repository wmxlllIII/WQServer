package com.example.test.pojo.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineMsg {
    @ApiModelProperty("离线消息表主键ID")
    private int messageId;

    @ApiModelProperty("发送方ID")
    private String senderId;

    @ApiModelProperty("接收方ID")
    private String receiverId;

    @ApiModelProperty("群ID")
    private int groupId;

    @ApiModelProperty("消息类型:文本、语音、视频、照片")
    private int msgType;

    @ApiModelProperty("文本，文件url")
    private String content;

    private long createTime;

    private long expiryTime;

    private boolean isDelivered;

    private boolean isDelete;
    public static final int TYPE_TEXT = 0;    // 文字
    public static final int TYPE_IMAGE = 1;   // 图片
    public static final int TYPE_VOICE = 2;   // 语音
    public static final int TYPE_VIDEO = 3;   // 视频
    public static final int TYPE_FILE = 4;    // 其他文件（如文档）
}
