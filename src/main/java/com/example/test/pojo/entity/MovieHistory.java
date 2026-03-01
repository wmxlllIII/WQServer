package com.example.test.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieHistory {
    private int id;
    private int movieId;
    private long userId;
    private int watchCount;
    private int progress;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
