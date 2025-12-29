package com.nihal.Music.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayListCreateRequestDto {

    @NotBlank(message = "Playlist name is required")
    @Size(min = 1, max = 20)
    private String name;
    
    private Set<String> song = new HashSet<>();

    private Long userId;


}
