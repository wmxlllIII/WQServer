package com.example.test.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorVO {
    private int actorId;
    private String actorName;
    private String actorIntro;
    private String actorGender;
    private String actorAvatar;
}
