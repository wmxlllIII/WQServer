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
@ApiModel(description = "用户登录返回的数据格式")
public class UserLoginVO implements Serializable {

    //TODO

    @ApiModelProperty("主键")
    private String uuid;

    @ApiModelProperty("用户邮箱")
    private String email;

    @ApiModelProperty("姓名")
    private String name;


    @ApiModelProperty("头像地址")
    private String avatarUrl;

    @ApiModelProperty("jwt令牌")
    private String token;

    @ApiModelProperty("用户数字id")
    private BigInteger uuNumber;

}
