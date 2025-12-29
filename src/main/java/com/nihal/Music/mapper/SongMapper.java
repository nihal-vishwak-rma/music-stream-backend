package com.nihal.Music.mapper;

import com.nihal.Music.dtos.SongDto;
import com.nihal.Music.entity.Song;
import org.springframework.stereotype.Component;

@Component
public class SongMapper {

    public SongDto toSongDto(Song song) {
        return SongDto.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .thumbnailUrl(song.getThumbnailUrl())
                .duration(song.getDuration())
                .videoId(song.getVideoId())
                .build();
    }
}
