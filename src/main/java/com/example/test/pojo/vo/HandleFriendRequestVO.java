package com.example.test.pojo.vo;

import com.example.test.pojo.entity.FriendRelationship;
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

    @ApiModelProperty("好友关系")
    private FriendRelationship friendRelationship;

    @ApiModelProperty("申请者信息")
    private UserVO user;

}
