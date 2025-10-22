package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "搜索用户数据传递类型")
public class SearchUserDTO {
    @ApiModelProperty("目标邮箱账号")
    private String email;

    @ApiModelProperty("目标电话号")
    private String phone;

    @ApiModelProperty("目标用户唯一数字id")
    private String uuNumber;


}
