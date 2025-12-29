package com.nihal.Music.services.Impl;

import com.nihal.Music.dtos.*;
import com.nihal.Music.entity.Song;
import com.nihal.Music.entity.User;
import com.nihal.Music.entity.UserPlayList;
import com.nihal.Music.exception.*;
import com.nihal.Music.mapper.SongMapper;
import com.nihal.Music.mapper.UserMapper;
import com.nihal.Music.repositories.SongRepository;
import com.nihal.Music.repositories.UserPlayListRepository;
import com.nihal.Music.repositories.UserRepository;
import com.nihal.Music.services.PlayListService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayListServiceImpl implements PlayListService {

    private final UserPlayListRepository userPlayListRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final MusicServiceImpl musicService;
    private final UserMapper userMapper;
    private final SongMapper songMapper;


    // generate collab playlist join code

    @Override
    public String generateCode(Long playListid) {

        return playListid.toString() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }


    // create private playlist 

    @Transactional
    @Override
    public PlayListResponseDto createPrivatePlaylist(PlayListCreateRequestDto playListCreateRequestDto) {

        User user = userRepository.findById(playListCreateRequestDto.getUserId())
                .orElseThrow(() -> new ResouceNotFound("user not found"));

        Optional<UserPlayList> userPlayListExists = userPlayListRepository
                .findByUserIdAndName(playListCreateRequestDto.getUserId(), playListCreateRequestDto.getName());

        if (userPlayListExists.isPresent()) {
            throw new DuplicateFormatFlagsException("Playlist with this name already exists");
        }


        Set<Song> avilablesongs = playListCreateRequestDto
                .getSong()
                .stream().map(videoId -> songRepository
                        .findByVideoId(videoId)
                        .orElseGet(() -> {
                            try {
                                return musicService.fetchAndSaveSong(videoId);
                            } catch (SongFetchException | IOException e) {
                                throw new SongFetchException(videoId, e);
                            }
                        }))
                .collect(Collectors.toSet());

        UserPlayList userPlayList = UserPlayList
                .builder()
                .name(playListCreateRequestDto.getName())
                .user(user)
                .build();

        userPlayList = userPlayListRepository.save(userPlayList);

        userPlayList.setSongs(avilablesongs);


        userPlayListRepository.save(userPlayList);

        Set<SongDto> songDtos = userPlayList.getSongs()
                .stream()
                .map(songMapper::toSongDto)
                .collect(Collectors.toSet());


        return PlayListResponseDto.builder()
                .name(userPlayList.getName())
                .user(userMapper.userToUserDto(userPlayList.getUser()))
                .songs(songDtos)
                .build();
    }


    // add songs in specific playlist

    @Transactional
    @Override
    public PlayListResponseDto addSongInPlaylis(PlayListUpdateSongsDto playListUpdateSongsDto) {

        User user = userRepository.findById(playListUpdateSongsDto.getUserId())
                .orElseThrow(() -> new ResouceNotFound("user not found"));


        UserPlayList playList = userPlayListRepository.findById(playListUpdateSongsDto.getId())
                .orElseThrow(() -> new ResouceNotFound("Playlist not found"));

        boolean isOwner = playList.getUser().getId().equals(user.getId());

        boolean isCollaborator = playList.getCollaborators().contains(user);

        if (!isOwner && !isCollaborator) {
            throw new ResouceNotFound("You are not allowed to modify this playlist");
        }


        Set<Song> newSongs = playListUpdateSongsDto.getSong()
                .stream()
                .map(videoId -> songRepository.findByVideoId(videoId)
                        .orElseGet(() -> {
                            try {
                                return musicService.fetchAndSaveSong(videoId);
                            } catch (Exception e) {
                                throw new SongFetchException(videoId, e);
                            }
                        })
                ).collect(Collectors.toSet());


        playList.getSongs().addAll(newSongs);

        userPlayListRepository.save(playList);

        Set<SongDto> songDtos = playList.getSongs()
                .stream()
                .map(songMapper::toSongDto)
                .collect(Collectors.toSet());


        return PlayListResponseDto.builder()
                .name(playList.getName())
                .user(userMapper.userToUserDto(playList.getUser()))
                .songs(songDtos)
                .build();
    }


    // get all user playlists 

    @Override
    public List<PlayListDto> getAllPlayList(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<PlayListDto> myPlayList = userPlayListRepository.findPlaylistsWithSongCount(userId);

        return myPlayList;

    }


    // get all songs from playlist

    @Override
    public List<SongDto> getAllSongsFromPlayList(Long playListId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResouceNotFound("User not found"));

        UserPlayList playList = userPlayListRepository.findById(playListId)
                .orElseThrow(() -> new ResouceNotFound("PlayList not found"));


        boolean isOwner = playList.getUser().getId().equals(user.getId());

        boolean isCollaborator = playList.getCollaborators().contains(user);

        if (!isOwner && !isCollaborator) {
            throw new ResouceNotFound("You don't have access to this playlist");
        }


        return userPlayListRepository.findAllSongsByPlaylistId(playListId);
    }


    // delete playlist your own playlist

    @Override
    @Transactional
    public List<PlayListDto> deletePlayLIst(Long playListId, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserPlayList playlist = userPlayListRepository.findById(playListId)
                .orElseThrow(() -> new ResouceNotFound("Playlist not exists !!"));

        if (playlist.getUser().getId() == userId) {
            userPlayListRepository.deleteById(playlist.getId());
        } else {
            throw new AccessDeniedException("only playlist admin can delete this playlist");
        }


        return getAllPlayList(userId);
    }


    @Override
    @Transactional
    public List<SongDto> deleteSongFromPlayList(Long userId, Long playListId, Long songId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResouceNotFound("user not found"));

        UserPlayList playList = userPlayListRepository.findById(playListId)
                .orElseThrow(() -> new ResouceNotFound("Playlist not found"));

        boolean isOwner = playList.getUser().getId().equals(user.getId());

        boolean isCollaborator = playList.getCollaborators().contains(user);

        if (!isOwner && !isCollaborator) {
            throw new ResouceNotFound("You don't have access to this playlist");
        }


        int status = userPlayListRepository.deleteSongFromPlaylist(playListId, songId);

        if (status > 0) {
            log.info("delete song successfully");
        } else {
            throw new ResouceNotFound("Song not found in this playlist");
        }


        return getAllSongsFromPlayList(playListId, userId);
    }

    @Override
    @Transactional
    public PlayListResponseDto createCollabPlayList(PlayListCreateRequestDto playListRequestDto) {

        User user = userRepository.findById(playListRequestDto.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<UserPlayList> userPlayList = userPlayListRepository
                .findByUserIdAndName(playListRequestDto.getUserId(), playListRequestDto.getName());

        if (userPlayList.isPresent()) {
            throw new DuplicateFormatFlagsException("Playlist already exists with same name");
        }


        Set<Song> userSongs = new HashSet<>();

        if (playListRequestDto.getSong() != null) {

            userSongs = playListRequestDto.getSong()
                    .stream().map(song -> songRepository
                            .findByVideoId(song)
                            .orElseGet(() -> {

                                try {
                                    return musicService.fetchAndSaveSong(song);

                                } catch (Exception e) {
                                    throw new SongFetchException("Failed to fetch song: " + song, e);
                                }

                            })).collect(Collectors.toSet());

        }


        Set<User> collaborator = new HashSet<>();

        UserPlayList newPlaylist = UserPlayList.builder()
                .name(playListRequestDto.getName())
                .user(user)
                .songs(userSongs)
                .collaborators(collaborator)
                .code(null)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        newPlaylist = userPlayListRepository.save(newPlaylist);

        newPlaylist.setCode(generateCode(newPlaylist.getId()));

        newPlaylist = userPlayListRepository.save(newPlaylist);

        Set<SongDto> songDtos = newPlaylist.getSongs()
                .stream()
                .map(songMapper::toSongDto)
                .collect(Collectors.toSet());


        return PlayListResponseDto.builder()
                .name(newPlaylist.getName())
                .user(userMapper.userToUserDto(newPlaylist.getUser()))
                .songs(songDtos)
                .build();
    }


    @Override
    @Transactional(dontRollbackOn = CodeExpiredException.class)
    public PlayListJoinResponseDto applyCode(@NotNull PlayListCode playListCode, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String code = playListCode.getCode();

        String[] splitCode = code.split("-", 2);

        if (splitCode.length < 2) {
            throw new CollabCodeException("Invalid code format");
        }


        UserPlayList userPlayList = userPlayListRepository.findById(Long.valueOf(splitCode[0]))
                .orElseThrow(() -> new ResouceNotFound("PlayList not exists"));


        if (userPlayList.getCode() == null) {
            throw new ResouceNotFound("No code generated for this playlist");
        }


        if (!userPlayList.isCodeValid(code)) {

            log.info("old code expired + " + userPlayList.getCode());

            String reGenerateCode = generateCode(userPlayList.getId());
            userPlayList.setCode(reGenerateCode);
            userPlayList.setExpiryTime(LocalDateTime.now().plusMinutes(5));

            log.info("new code generated + " + userPlayList.getCode());

            userPlayListRepository.saveAndFlush(userPlayList);


            throw new CodeExpiredException("Code expired, please use new one");
        }


        if (!userPlayList.getCollaborators().contains(user)) {
            userPlayList.getCollaborators().add(user);
        }

        userPlayList = userPlayListRepository.save(userPlayList);


        return PlayListJoinResponseDto.builder()
                .playlistId(userPlayList.getId())
                .playlistName(userPlayList.getName())
                .joined(true)
                .message("Joined successfully in Playlist")
                .build();
        
    }


    @Override
    public List<PlayListDto> getAllCollabPlayList(Long userId) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResouceNotFound("User not found"));


        return userPlayListRepository.findCollabPlaylistsWithSongCount(userId);
    }


    // user remove from collab playlist

    @Override
    @Transactional
    public List<PlayListDto> removeFromPlayList(Long userId, Long playlistId) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResouceNotFound("User not found"));

        UserPlayList playList = userPlayListRepository.findById(playlistId)
                .orElseThrow(() -> new ResouceNotFound("Playlist Not Found"));

        boolean isCollaborator = playList.getCollaborators()
                .stream().anyMatch(user -> user.getId().equals(currentUser.getId()));

        if (!isCollaborator) {
            throw new ResouceNotFound("You are Not member of This Playlist");
        }

        playList.getCollaborators().removeIf(user -> user.getId().equals(currentUser.getId()));

        userPlayListRepository.save(playList);


        return userPlayListRepository.findCollabPlaylistsWithSongCount(userId);
    }


}
