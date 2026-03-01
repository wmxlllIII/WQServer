package com.example.test.pojo.entity;

import com.example.test.common.utils.TimeUtil;
import com.example.test.pojo.vo.FriendRelationshipVO;
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

    private int status;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;


    public FriendRelationshipVO toVO() {
        return FriendRelationshipVO.builder()
                .id(this.getId())
                .senderId(this.getSenderId())
                .receiverId(this.getReceiverId())
                .validMsg(this.getValidMsg())
                .status(this.getStatus())
                .createAt(TimeUtil.dateTimeToSecond(this.getCreateAt()))
                .updateAt(TimeUtil.dateTimeToSecond(this.getUpdateAt()))
                .build();
    }
}
