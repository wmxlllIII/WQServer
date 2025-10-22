package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "获取声网token数据模型")
public class AgoraTokenDTO {
    private String channelName;
    private String userId;
    private int role;
    private int expire;
}
