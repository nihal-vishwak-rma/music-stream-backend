package com.nihal.Music.repositories;

import com.nihal.Music.dtos.PlayListDto;
import com.nihal.Music.dtos.SongDto;
import com.nihal.Music.entity.UserPlayList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPlayListRepository extends JpaRepository<UserPlayList, Long> {

    Optional<UserPlayList> findByUserIdAndName(Long userId, String name);

    // for personal playlist
    @Query("SELECT DISTINCT new com.nihal.Music.dtos.PlayListDto(p.id, p.name, size(p.songs) , p.code) " +
            "FROM UserPlayList p " +
            "WHERE p.user.id = :userId ")
    List<PlayListDto> findPlaylistsWithSongCount(@Param("userId") Long userId);

    // for collab PlayList

    @Query("SELECT DISTINCT new com.nihal.Music.dtos.PlayListDto(p.id, p.name, size(p.songs), p.code) " +
            "FROM UserPlayList p " +
            "JOIN p.collaborators c " +
            "WHERE c.id = :userId AND p.user.id <> :userId")
    List<PlayListDto> findCollabPlaylistsWithSongCount(@Param("userId") Long userId);


    @Query("SELECT new com.nihal.Music.dtos.SongDto(" +
            "s.id, s.videoId, s.title, s.artist, s.duration, s.thumbnailUrl) " +
            "FROM UserPlayList p " +
            "JOIN p.songs s " +
            "WHERE p.id = :playlistId")
    List<SongDto> findAllSongsByPlaylistId(@Param("playlistId") Long playlistId);

    Optional<UserPlayList> findByUser_IdAndId(Long userId, Long playLIstId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM playlist_songs WHERE playlist_id = :playlistId AND song_id = :songId", nativeQuery = true)
    int deleteSongFromPlaylist(@Param("playlistId") Long playlistId, @Param("songId") Long songId);


}