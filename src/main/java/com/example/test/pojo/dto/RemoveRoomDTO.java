package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "销毁房间数据模型")
public class RemoveRoomDTO {
    @ApiModelProperty("房间号")
    private String roomId;

}
