package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "被申请方处理好友请求传递数据类型")
public class HandleFriendRequestDTO {
    @ApiModelProperty("请求者")
    private String requestEmail;


    @ApiModelProperty("是否同意")
    private boolean isAgree;
}
