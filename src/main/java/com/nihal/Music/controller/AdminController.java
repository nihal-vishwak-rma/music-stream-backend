package com.nihal.Music.controller;


import com.nihal.Music.dtos.ApiResponse;
import com.nihal.Music.dtos.PlayListDto;
import com.nihal.Music.dtos.SongDto;
import com.nihal.Music.dtos.UserDto;
import com.nihal.Music.security.MusicUserDetails;
import com.nihal.Music.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasAuthority(T(com.nihal.Music.entity.role.Roles).ADMIN.name())")

@Tag(name = "Admin Controller", description = "Administrative APIs for managing users, playlists, and songs. Requires ADMIN role.")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {


    private final AdminService adminService;

    /**
     * Retrieves all users in the system.
     * This endpoint is cached to improve performance for frequently accessed data.
     *
     * @param musicUserDetails authenticated admin user details
     * @return List of all users with their information
     */


    @Cacheable
    @GetMapping("/getAllUsers")
    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all registered users in the system. This operation is cached for performance optimization."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserDto.class))
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers(@AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        List<UserDto> users = adminService.getAllUsers(musicUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, users, users.isEmpty() ? "Users not exists" : "Fetched All users successfully"));
    }


    /**
     * Switch user role between ADMIN and USER.
     *
     * @param userId           target user ID whose role needs to be switched
     * @param musicUserDetails authenticated admin user details
     * @return Updated user information with new role
     */

    @PostMapping("/switchRole/{userId}")
    @Operation(
            summary = "Switch user role",
            description = "Toggles user role between ADMIN and USER. Only admins can perform this operation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User role switched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found with the provided ID",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid user ID provided",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<UserDto>> switchRole(@Parameter(description = "ID of the user whose role needs to be switched", required = true, example = "1")
                                                           @PathVariable Long userId,
                                                           @Parameter(hidden = true) @AuthenticationPrincipal MusicUserDetails musicUserDetails) {


        UserDto user = adminService.switchRole(userId, musicUserDetails);


        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, user, "Role Successfully switched"));

    }

    /**
     * View all private playlists of a specific user.
     *
     * @param userId           target user ID whose private playlists to retrieve
     * @param musicUserDetails authenticated admin user details
     * @return List of user's private playlists
     */


    @GetMapping("/private-Playlist/{userId}")
    @Operation(
            summary = "View user's private playlists",
            description = "Retrieves all private playlists belonging to a specific user. Only admins can view private playlists of other users."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Private playlists retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PlayListDto.class))
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found or no private playlists exist",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content
            )
    })

    public ResponseEntity<ApiResponse<List<PlayListDto>>> viewPrivatePlayList(@Parameter(description = "ID of the user whose private playlists to retrieve", required = true, example = "1")
                                                                              @PathVariable Long userId
            , @Parameter(hidden = true) @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        List<PlayListDto> playlists = adminService.allPrivatePlaylist(userId, musicUserDetails);


        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, playlists, playlists.isEmpty() ? " private playlist not found" : "Fetched user private playlist Successfully "));

    }


    /**
     * View all collaborative playlists of a specific user.
     *
     * @param userId           target user ID whose collaborative playlists to retrieve
     * @param musicUserDetails authenticated admin user details
     * @return List of user's collaborative playlists
     */

    @GetMapping("/collab-Playlist/{userId}")
    @Operation(
            summary = "View user's collaborative playlists",
            description = "Retrieves all collaborative playlists that a specific user is part of or owns."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Collaborative playlists retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PlayListDto.class))
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found or no collaborative playlists exist",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<List<PlayListDto>>> viewCollabPlayList(@Parameter(description = "ID of the user whose collaborative playlists to retrieve", required = true, example = "1")
                                                                             @PathVariable Long userId
            , @Parameter(hidden = true) @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        List<PlayListDto> allplaylist = adminService.allCollabPlaylist(userId, musicUserDetails);


        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, allplaylist, allplaylist.isEmpty() ? " Collab playlist not found" : "Fetched user collab playlist Successfully "));

    }


    /**
     * View all songs in a specific playlist of a user.
     *
     * @param userId           target user ID who owns the playlist
     * @param playListId       target playlist ID whose songs to retrieve
     * @param musicUserDetails authenticated admin user details
     * @return List of songs in the specified playlist
     */

    @GetMapping("/user/{userId}/playlist/{playListId}")
    @Operation(
            summary = "View songs in user's playlist",
            description = "Retrieves all songs from a specific playlist belonging to a user. Admins can view any user's playlist content."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Playlist songs retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SongDto.class))
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User, playlist not found, or playlist is empty",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid user ID or playlist ID provided",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<List<SongDto>>> viewSongs(@Parameter(description = "ID of the user who owns the playlist", required = true, example = "1")
                                                                @Valid @PathVariable Long userId,
                                                                @Parameter(description = "ID of the playlist whose songs to retrieve", required = true, example = "1")
                                                                @Valid @PathVariable Long playListId,
                                                                @Parameter(hidden = true) @AuthenticationPrincipal MusicUserDetails musicUserDetails) {

        List<SongDto> songs = adminService.playlistSongs(userId, playListId, musicUserDetails);

        return ResponseEntity
                .ok(new ApiResponse<>(true, songs, songs.isEmpty() ? "No songs available in this playlist" : "All songs fetched successfully"));
    }


    /**
     * Delete a user from the system.
     * This action is irreversible and will remove all user data including playlists and associations.
     *
     * @param userId           target user ID to be deleted
     * @param musicUserDetails authenticated admin user details
     * @return Confirmation message of successful deletion
     */

    @DeleteMapping("/delete/{userId}")
    @Operation(
            summary = "Delete user account",
            description = "Permanently deletes a user account and all associated data (playlists, songs, etc.). This action cannot be undone."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.nihal.Music.dtos.ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found with the provided ID",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid user ID or cannot delete admin user",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<Void>> removeUser(@Parameter(description = "ID of the user to be deleted", required = true, example = "1")
                                                        @Valid @PathVariable Long userId
            , @Parameter(hidden = true) @AuthenticationPrincipal MusicUserDetails musicUserDetails) {


        adminService.removeUser(userId, musicUserDetails);

        return ResponseEntity.ok(new ApiResponse<>(true, null, "User with ID " + userId + " deleted successfully"));
    }


}
