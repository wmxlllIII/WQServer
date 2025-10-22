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
    @ApiModelProperty("昵称")
    private String username;


    @ApiModelProperty("头像")
    private String avatarUrl;

    @ApiModelProperty("邮箱")
    private String email;
}
