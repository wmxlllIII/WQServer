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
public class FriendRelationship {


    private int id;


    private long senderId;


    private long receiverId;


    private String validMsg;


    private String status;


    private LocalDateTime createAt;


    private LocalDateTime updateAt;


}
