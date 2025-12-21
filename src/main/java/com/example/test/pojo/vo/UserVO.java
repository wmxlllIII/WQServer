package com.example.test.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private long uuNumber;

    private String username;

    private String avatarUrl;

    private String email;

    private long updateAt;
}
