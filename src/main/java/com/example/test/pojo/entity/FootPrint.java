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
public class FootPrint {
    private int id;
    private int postId;
    private long userId;
    private int viewCount;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
