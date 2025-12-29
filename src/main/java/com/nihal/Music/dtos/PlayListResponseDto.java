package com.nihal.Music.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class PlayListResponseDto {

    private String name;
    private UserDto user;
    private Set<SongDto> songs;
}
