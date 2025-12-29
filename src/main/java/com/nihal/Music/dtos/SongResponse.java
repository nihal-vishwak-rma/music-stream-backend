package com.nihal.Music.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongResponse {

    private List<SongDto> songs = new ArrayList<>();
    private String nextPageToken;
    private String prevPageToken;
}
