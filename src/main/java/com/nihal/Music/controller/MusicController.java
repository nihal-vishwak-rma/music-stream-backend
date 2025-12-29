package com.nihal.Music.controller;

import com.nihal.Music.dtos.ApiResponse;
import com.nihal.Music.dtos.SongResponse;
import com.nihal.Music.dtos.StreamUrlDto;
import com.nihal.Music.entity.User;
import com.nihal.Music.repositories.UserRepository;
import com.nihal.Music.security.MusicUserDetails;
import com.nihal.Music.services.MusicService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/music")

@Tag(name = "Music Controller", description = "Api for Music")
public class MusicController {

    private final MusicService musicService;
    private final UserRepository userRepository;

    @GetMapping("/search")
    @Cacheable
    public ResponseEntity<ApiResponse<SongResponse>> searchSongs(@RequestParam String songname,
                                                                 @RequestParam(required = false) String pagetoken,
                                                                 @AuthenticationPrincipal MusicUserDetails musicUserDetails) throws IOException {

        Long userId = musicUserDetails.getUser().getId();

        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (songname == null || songname.trim().isEmpty() || "null".equals(songname)) {
            songname = "latest songs hindi";
        }

        SongResponse songs = musicService.searchSongNewPipe(songname, pagetoken, 50);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, songs,
                songs.getSongs().isEmpty() ? "No songs found " : "Songs successfully found"));

    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<ApiResponse<StreamUrlDto>> getStreamUrl(@PathVariable String videoId, @RequestParam(defaultValue = "medium") String quality) {


        StreamUrlDto url = musicService.extractStreamUrl(videoId, quality);

        return ResponseEntity.ok(new ApiResponse<>(true, url, "Stream url found successfully"));

    }


}
