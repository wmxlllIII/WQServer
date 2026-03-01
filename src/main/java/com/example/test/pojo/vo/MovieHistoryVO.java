package com.example.test.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "电影类别返回数据类型")
public class MovieHistoryVO {
    private int movieId;
    private long userId;
    private int watchCount;
    private int progress;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
