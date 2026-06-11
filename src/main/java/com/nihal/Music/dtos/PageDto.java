package com.nihal.Music.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageDto {

    private String url;
    private Map<String, String> cookies;

}
