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
public class User {

    private String username;

    private String avatarUrl;

    private long uuNumber;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

}
