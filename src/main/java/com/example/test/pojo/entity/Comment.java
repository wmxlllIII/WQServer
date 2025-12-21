package com.example.test.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    private int id;

    private int postId;

    private long userId;

    private long replyToUserId;

    private int parentId;

    private String content;

    private Timestamp createAt;

}
