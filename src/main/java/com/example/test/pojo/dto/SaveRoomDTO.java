package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "保存房间数据模型")
public class SaveRoomDTO {
    @ApiModelProperty("房间号")
    private String roomId;

    @ApiModelProperty("电影id")
    private int movieId;
}
