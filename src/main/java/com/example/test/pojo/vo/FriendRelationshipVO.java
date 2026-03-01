package com.example.test.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendRelationshipVO {

    private int id;


    private long senderId;


    private long receiverId;


    private String validMsg;


    private int status;


    private long createAt;


    private long updateAt;

}
