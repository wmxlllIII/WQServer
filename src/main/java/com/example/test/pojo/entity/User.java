package com.example.test.pojo.entity;


import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String uuid;


    private String username;


    private String avatarUrl;


    private String email;


    private String phone;


    private BigInteger uuNumber;


    private String password;


    private Boolean emailVerified = false;


    private String createAt;


    private String updateAt;


    private Integer version;
}
