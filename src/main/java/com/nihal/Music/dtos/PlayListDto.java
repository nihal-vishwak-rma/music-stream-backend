package com.nihal.Music.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayListDto {

    private Long id;
    private String name;
    private long totalsongs;
    private String code;

}
