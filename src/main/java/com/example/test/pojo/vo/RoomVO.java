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
@ApiModel(description = "获取房间列表返回数据类型")
public class RoomVO {

    @ApiModelProperty("房间id")
    private String roomId;

    @ApiModelProperty("电影封面")
    private String movieCover;

    @ApiModelProperty("电影名")
    private String movieName;

    @ApiModelProperty("影片地址")
    private String movieUrl;
}
