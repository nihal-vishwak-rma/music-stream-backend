package com.nihal.Music.services.Impl;

import com.nihal.Music.dtos.PlayListDto;
import com.nihal.Music.dtos.SongDto;
import com.nihal.Music.dtos.UserDto;
import com.nihal.Music.entity.User;
import com.nihal.Music.entity.UserPlayList;
import com.nihal.Music.entity.role.Roles;
import com.nihal.Music.exception.AccessDeniedException;
import com.nihal.Music.exception.ResouceNotFound;
import com.nihal.Music.mapper.UserMapper;
import com.nihal.Music.repositories.UserPlayListRepository;
import com.nihal.Music.repositories.UserRepository;
import com.nihal.Music.security.MusicUserDetails;
import com.nihal.Music.services.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserPlayListRepository userPlayListRepository;


//    private void validateAdmin(MusicUserDetails admin) {
//        boolean isAdmin = admin.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
//        if (!isAdmin) {
//            throw new AccessDeniedException("You are not authorized");
//        }
//    }


    @Override
    public List<UserDto> getAllUsers(MusicUserDetails admin) {


        List<User> users = userRepository.findAll();


        return users.stream()
                .map(u -> new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole())).toList();
    }


    @Override
    @Transactional
    public UserDto switchRole(Long userId, MusicUserDetails admin) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResouceNotFound("User not found"));


        if (user.getId().equals(admin.getUser().getId())) {
            throw new AccessDeniedException("You cannot change your own role");
        }


        if (user.getRole().equals(Roles.ADMIN)) {
            user.setRole(Roles.USER);
        } else {
            user.setRole(Roles.ADMIN);
        }

        userRepository.save(user);


        return userMapper.userToUserDto(user);
    }


    @Override
    public List<PlayListDto> allPrivatePlaylist(Long userId, MusicUserDetails admin) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return userPlayListRepository.findPlaylistsWithSongCount(userId);

    }


    @Override
    public List<PlayListDto> allCollabPlaylist(Long userId, MusicUserDetails admin) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return userPlayListRepository.findCollabPlaylistsWithSongCount(userId);

    }


    @Override
    public List<SongDto> playlistSongs(Long userId, Long playListId, MusicUserDetails admin) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserPlayList playList = userPlayListRepository.findById(playListId)
                .orElseThrow(() -> new ResouceNotFound("PlayList not found"));


        return userPlayListRepository.findAllSongsByPlaylistId(playListId);
    }


    @Override
    @Transactional
    public void removeUser(Long userId, MusicUserDetails admin) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("No user found"));

        if (user.getId().equals(admin.getUser().getId())) {
            throw new AccessDeniedException("You cannot delete your own account");
        }


        userRepository.delete(user);

    }


}
