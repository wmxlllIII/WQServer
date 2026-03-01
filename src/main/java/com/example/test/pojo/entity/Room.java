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
public class Room {
    private int id;

    private String roomId;

    private int movieId;

    private long ownerId;

    private int status;

    private LocalDateTime createAt;
}
