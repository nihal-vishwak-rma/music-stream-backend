package com.nihal.Music.services;

import com.nihal.Music.dtos.*;
import com.nihal.Music.exception.CodeExpiredException;

import java.util.List;

public interface PlayListService {

    PlayListResponseDto createPrivatePlaylist(PlayListCreateRequestDto playListRequestDto);

    PlayListResponseDto addSongInPlaylis(PlayListUpdateSongsDto playListUpdateSongsDto);

    List<PlayListDto> getAllPlayList(Long userId);

   List<SongDto> getAllSongsFromPlayList(Long PlayListId , Long userId);

   List<PlayListDto> deletePlayLIst(Long playListId , Long userId);

   List<SongDto> deleteSongFromPlayList(Long userId , Long playListId , Long songId);

   PlayListResponseDto createCollabPlayList(PlayListCreateRequestDto playListRequestDto);


  // generate code for collaborative playlist
   String generateCode(Long playListid);


   PlayListJoinResponseDto applyCode(PlayListCode code , Long userId) throws CodeExpiredException;


   List<PlayListDto> getAllCollabPlayList(Long userId);

   List<PlayListDto> removeFromPlayList(Long userId , Long playlistId);

}
