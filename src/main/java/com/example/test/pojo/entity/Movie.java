package com.example.test.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    private int id;

    private String movieName;


    private String movieUrl;


    private double movieLength;


    private String movieCover;


    private String movieActors;


}
