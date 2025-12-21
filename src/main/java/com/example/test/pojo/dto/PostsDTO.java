package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@ApiModel(description = "用户发布帖子传递数据模型")
public class PostsDTO {

    @ApiModelProperty("帖子标题")
    private String title;

    @ApiModelProperty("帖子内容")
    private String content;

    @ApiModelProperty("帖子图片Url")
    private List<String> images;
}
