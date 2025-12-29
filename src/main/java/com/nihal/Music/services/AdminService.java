package com.nihal.Music.services;

import com.nihal.Music.dtos.PlayListDto;
import com.nihal.Music.dtos.SongDto;
import com.nihal.Music.dtos.UserDto;
import com.nihal.Music.security.MusicUserDetails;

import java.util.List;

public interface AdminService {


    List<UserDto> getAllUsers(MusicUserDetails admin);

    UserDto switchRole(Long userId , MusicUserDetails admin);

    List<PlayListDto> allPrivatePlaylist(Long userId , MusicUserDetails admin);

    List<PlayListDto> allCollabPlaylist(Long userId , MusicUserDetails admin);

    List<SongDto> playlistSongs(Long userId , Long playListId , MusicUserDetails admin);

    void removeUser(Long userId , MusicUserDetails admin);

}
