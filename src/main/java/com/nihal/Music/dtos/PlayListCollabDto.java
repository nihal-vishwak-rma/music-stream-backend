package com.nihal.Music.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class PlayListCollabDto {

    private String name;
    private UserDto creator;
    private Set<SongDto> songs;
    private Set<UserDto> collaborators;

}
