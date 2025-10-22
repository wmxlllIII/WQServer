package com.example.test.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRelationship {


    private int id;


    private String senderId;


    private String receiverId;


    private String validMsg;


    private String status;


    private String createAt;


    private String updateAt;


}
