package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "保存进度传递数据模型")
public class SaveProgressDTO {
    private int movieId;
    private int currentProgress;
}
