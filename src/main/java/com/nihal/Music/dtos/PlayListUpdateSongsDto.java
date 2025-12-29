package com.nihal.Music.dtos;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayListUpdateSongsDto {

    @NotNull(message = "Playlist id is required")
    private Long id;
    private Set<String> song;
    private Long userId;

}
