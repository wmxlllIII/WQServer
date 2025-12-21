package com.example.test.pojo.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户注册返回的数据格式")
public class RegisterVO implements Serializable {


    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("用户数字id")
    private long uuNumber;

    @ApiModelProperty("认证Token")
    private String token;

    @ApiModelProperty("头像URL")
    private String avatarUrl;

    @ApiModelProperty("账号状态")
    private String status;

    @ApiModelProperty("邮箱是否已验证")
    private Boolean emailVerified;

    @ApiModelProperty("注册时间")
    private String registerTime;

    @ApiModelProperty("状态码")
    private Integer code;

    @ApiModelProperty("提示信息")
    private String message;

}
