package com.example.test.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "电影信息返回数据类型")
public class MovieVO {
    private int id;

    private String movieName;


    private String movieUrl;


    private double movieLength;


    private String movieCover;


    private List<ActorVO> movieActors;
}
