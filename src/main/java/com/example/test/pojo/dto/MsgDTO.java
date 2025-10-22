package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class MsgDTO {
    @ApiModelProperty("接收方")
    private String targetEmail;

    @ApiModelProperty("消息内容")
    private String msg;


}
