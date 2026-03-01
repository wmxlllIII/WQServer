package com.example.test.pojo.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "获取演员个人信息数据模型")
public class ActorProfileDTO {
    private int actorId;
}
