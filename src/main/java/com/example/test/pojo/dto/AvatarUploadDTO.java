package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@ApiModel(description = "用户上传头像数据模型")
public class AvatarUploadDTO {


    @ApiModelProperty("头像文件")
    private MultipartFile file;

    @ApiModelProperty("图片描述")
    private String description;
}
