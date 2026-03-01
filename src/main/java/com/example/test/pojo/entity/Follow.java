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
public class Follow {
    private int id;
    private long followerId;
    private long followedId;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
