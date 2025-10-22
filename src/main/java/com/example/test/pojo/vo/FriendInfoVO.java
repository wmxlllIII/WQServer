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
@ApiModel(description = "好友信息返回数据类型")
public class FriendInfoVO {

    @ApiModelProperty("昵称")
    private String userName;


    @ApiModelProperty("头像")
    private String avatarUrl;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("更新时间")
    private String updateAt;
}
