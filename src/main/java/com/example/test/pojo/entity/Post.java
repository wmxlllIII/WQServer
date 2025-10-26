package com.example.test.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private int id;

    private String userId;

    private String content;

    private String coverUrl;

    private int likeCount;

    private LocalDateTime createAt;

}
