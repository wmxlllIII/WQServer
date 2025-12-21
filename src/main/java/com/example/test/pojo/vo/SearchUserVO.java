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
@ApiModel(description = "搜索用户返回的数据格式")
public class SearchUserVO {
    @ApiModelProperty("是否为好友")
    private boolean isFriend;

    @ApiModelProperty("是否在黑名单")
    private boolean isInBlackList;

    @ApiModelProperty("用户信息")
    private FriendInfoVO FriendInfoVO;

}
