package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "用户获取验证码数据模型")
public class SendCodeDTO implements Serializable {

    @ApiModelProperty("邮箱")
    private String email;

}
