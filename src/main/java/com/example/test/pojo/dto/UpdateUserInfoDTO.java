package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户更新个人信息数据模型")
public class UpdateUserInfoDTO {

    @ApiModelProperty("用户昵称")
    private String userName;

}
