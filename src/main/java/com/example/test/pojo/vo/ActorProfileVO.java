package com.example.test.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorProfileVO {
    private int actorId;
    private String actorName;
    private String actorIntro;
    private int actorGender;
    private String actorAvatar;
}
