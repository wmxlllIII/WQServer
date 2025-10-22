package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "分页查询帖子传递数据模型")
public class PostsQueryDTO {
    @ApiModelProperty("当前页")
    private int page = 1;

    @ApiModelProperty("每页数量")
    private int size = 10;
}
