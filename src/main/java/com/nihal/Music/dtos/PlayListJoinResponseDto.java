package com.nihal.Music.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayListJoinResponseDto {

    private Long playlistId;
    private String playlistName;
    private boolean joined;
    private String message;

}
