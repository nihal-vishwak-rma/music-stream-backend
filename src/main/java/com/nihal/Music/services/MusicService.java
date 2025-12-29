package com.nihal.Music.services;


import com.nihal.Music.dtos.SongResponse;
import com.nihal.Music.dtos.StreamUrlDto;

import java.io.IOException;

public interface MusicService {

    SongResponse searchSongs(String query, String pagetoken) throws IOException;

    SongResponse searchSongNewPipe(String query, String pageToken, int limit);

    StreamUrlDto extractStreamUrl(String videoId, String quality);
}
