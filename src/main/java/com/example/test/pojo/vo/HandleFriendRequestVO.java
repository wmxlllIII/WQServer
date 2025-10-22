package com.example.test.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "被申请方处理申请返回数据类型")
public class HandleFriendRequestVO  {

    @ApiModelProperty("处理状态（accepted/rejected）")
    private String status;


    @ApiModelProperty("昵称")
    private String username;


    @ApiModelProperty("头像")
    private String avatarUrl;

    @ApiModelProperty("邮箱")
    private String email;
}
