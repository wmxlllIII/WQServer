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

    private String userId;

    private int parentId;

    private String replyToUserId;

    private String content;

    private Timestamp createAt;

}
