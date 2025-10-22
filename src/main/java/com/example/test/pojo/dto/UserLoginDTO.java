package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
@Data
@ApiModel(description = "用户登录传递数据模型")
public class UserLoginDTO implements Serializable {

    @ApiModelProperty("邮箱账号")
    private String email;

    @ApiModelProperty("密码")
    private String password;
}
