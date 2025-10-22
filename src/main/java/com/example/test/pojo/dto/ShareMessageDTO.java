package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户分享消息数据模型")
public class ShareMessageDTO {

    @ApiModelProperty("接收方")
    private String targetEmail;

    @ApiModelProperty("分享链接标题")
    private String linkTitle;

    @ApiModelProperty("分享链接描述")
    private String linkContent;

    @ApiModelProperty("分享图片URL")
    private String linkImageUrl;


}
