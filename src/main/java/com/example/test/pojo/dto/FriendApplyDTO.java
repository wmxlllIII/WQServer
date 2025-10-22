package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "申请好友数据传递数据类型")
public class FriendApplyDTO {
//    @ApiModelProperty("申请者邮箱")
//    private String userEmail;

    @ApiModelProperty("被申请邮箱")
    private String targetEmail;

    @ApiModelProperty("验证消息")
    private String validMsg;
}
