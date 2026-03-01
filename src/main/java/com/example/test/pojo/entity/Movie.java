package com.example.test.pojo.entity;

import com.example.test.common.utils.UrlUtil;
import com.example.test.pojo.vo.ActorVO;
import com.example.test.pojo.vo.MovieVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    private int id;

    private String movieName;


    private String movieUrl;


    private double duration;


    private String movieCover;


    private List<ActorVO> actors;


    public MovieVO toVO() {
        return MovieVO.builder()
                .id(id)
                .movieName(movieName)
                .movieUrl(UrlUtil.fillUrl(movieUrl))
                .movieLength(duration)
                .movieCover(UrlUtil.fillUrl(movieCover))
                .movieActors(actors)
                .build();
    }
}
