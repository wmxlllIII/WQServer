package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户发布帖子评论传递数据模型")
public class AddCommentDTO {
    @ApiModelProperty("帖子id")
    private int postId;

    @ApiModelProperty("父评论id，0 表示一级评论")
    private int parentId = -1;

    @ApiModelProperty("回复的用户id，一级评论可不传")
    private String replyToUserId;

    @ApiModelProperty("评论内容")
    private String content;
}
