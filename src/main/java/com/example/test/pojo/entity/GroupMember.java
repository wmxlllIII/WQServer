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
public class GroupMember {

    private int id;

    private int groupId;

    private long userId;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;
}
