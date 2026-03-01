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
public class Auth {
    private int authId;
    private long userId;
    private int authType;
    private String authValue;
    private String password;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
