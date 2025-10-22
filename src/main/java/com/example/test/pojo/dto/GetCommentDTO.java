package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "分页获取帖子评论传递数据类型")
public class GetCommentDTO {

    @ApiModelProperty("帖子id")
    private int postId;

    @ApiModelProperty("当前页")
    private int page = 1;

    @ApiModelProperty("每页数量")
    private int size = 15;
}
