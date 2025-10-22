package com.example.test.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户更新头像返回的数据格式")
public class UpdateAvatarVO {

    @ApiModelProperty("头像存储位置")
    private String avatarUrl;
}
