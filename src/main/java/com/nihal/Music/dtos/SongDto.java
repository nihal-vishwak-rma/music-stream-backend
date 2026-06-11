package com.nihal.Music.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongDto {

    private Long id;
    private String videoId;
    private String title;
    private String artist;
    private String duration;
    private String thumbnailUrl;


}
