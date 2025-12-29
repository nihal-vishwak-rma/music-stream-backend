package com.nihal.Music.dtos;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongDto {

    private Long id;
    private String videoId;       // YouTube video ID
    private String title;         // Song title
    private String artist;        // Channel name as artist
    private String duration;      // Duration in ISO 8601 format, e.g., PT3M30S
    private String thumbnailUrl;  // Default thumbnail URL


}
