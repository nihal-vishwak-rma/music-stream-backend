package com.nihal.Music.controller;

import com.nihal.Music.dtos.*;
import com.nihal.Music.security.MusicUserDetails;
import com.nihal.Music.services.PlayListService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/playlist")
@RequiredArgsConstructor

@Tag(name = "PlayList Controller", description = "Api for PlayList")
public class PlayListController {

    private final PlayListService playListService;


    @PostMapping("/createPrivate")
    public ResponseEntity<ApiResponse<PlayListResponseDto>> createPlayList(@Valid @RequestBody PlayListCreateRequestDto playListRequestDto
            , @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        Long userId = musicUserDetails.getUser().getId();

        playListRequestDto.setUserId(userId);

        PlayListResponseDto playlist = playListService.createPrivatePlaylist(playListRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, playlist, "playlist created successfully"));

    }

    @PostMapping("/addSongInPlayList")
    public ResponseEntity<ApiResponse<PlayListResponseDto>> addSongInPlayList(@Valid @RequestBody PlayListUpdateSongsDto playListUpdateSongsDto,
                                                                              @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        Long userId = musicUserDetails.getUser().getId();

        playListUpdateSongsDto.setUserId(userId);

        PlayListResponseDto responseDto = playListService.addSongInPlaylis(playListUpdateSongsDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, responseDto, "Song add successfully in playlist"));
    }


    @GetMapping("/myPlayList")
    public ResponseEntity<ApiResponse<List<PlayListDto>>> getAllPlayLists(@AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        Long id = musicUserDetails.getUser().getId();

        List<PlayListDto> allPlayLists = playListService.getAllPlayList(id);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, allPlayLists, allPlayLists.isEmpty() ? "No PlayList found" : "Fetched all your playlists successfully"));
    }

    @GetMapping("/collabPlayList")
    public ResponseEntity<ApiResponse<List<PlayListDto>>> getAllCollabPlayLists(@AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        Long id = musicUserDetails.getUser().getId();

        List<PlayListDto> allPlayLists = playListService.getAllCollabPlayList(id);


        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, allPlayLists, allPlayLists.isEmpty() ? "No PlayList found" : "Fetched all your playlists successfully"));
    }


    @GetMapping("/getPlayListSongs/{playListId}")
    public ResponseEntity<ApiResponse<List<SongDto>>> getAllPlayListSongs(@PathVariable Long playListId, @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        Long id = musicUserDetails.getUser().getId();


        List<SongDto> allsongs = playListService.getAllSongsFromPlayList(playListId, id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(true, allsongs, allsongs.isEmpty() ? "Playlist is empty" : "All songs found from playlist"));
    }

    @DeleteMapping("/delete/{playlistId}")
    public ResponseEntity<ApiResponse<List<PlayListDto>>> deletePlayList(@PathVariable Long playlistId, @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        Long id = musicUserDetails.getUser().getId();

        List<PlayListDto> allPlayLIst = playListService.deletePlayLIst(playlistId, id);


        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, allPlayLIst, "PlayList delete successfully"));

    }


    @DeleteMapping("/{playlistId}/song/{songId}")
    public ResponseEntity<ApiResponse<List<SongDto>>> deleteSongFromPlayList(@PathVariable Long playlistId, @PathVariable Long songId, @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        Long userid = musicUserDetails.getUser().getId();

        log.warn("user id " + userid);

        List<SongDto> allsong = playListService.deleteSongFromPlayList(userid, playlistId, songId);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, allsong, " Song delete successfully"));

    }


    @PostMapping("/createCollabPlayList")
    public ResponseEntity<ApiResponse<PlayListResponseDto>> createCollabPlayList(@Valid @RequestBody PlayListCreateRequestDto playListRequestDto, @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        Long userid = musicUserDetails.getUser().getId();

        playListRequestDto.setUserId(userid);

        PlayListResponseDto playlist = playListService.createCollabPlayList(playListRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, playlist, "Playlist Created Successfully"));
    }


    @PostMapping("/applycode")
    public ResponseEntity<ApiResponse<PlayListJoinResponseDto>> applyCode(@RequestBody PlayListCode playListCode
            , @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        Long userid = musicUserDetails.getUser().getId();

        PlayListJoinResponseDto playList = playListService.applyCode(playListCode, userid);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, playList, "Joined successfully in Playlist"));
    }


    @DeleteMapping("/remove/{playlistId}")
    public ResponseEntity<ApiResponse<List<PlayListDto>>> removeFromPlayList(@PathVariable Long playlistId,
                                                                             @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        List<PlayListDto> playLists = playListService.removeFromPlayList(musicUserDetails.getUser().getId(), playlistId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, playLists, "Removed from playlist successfully"));

    }

    


}
