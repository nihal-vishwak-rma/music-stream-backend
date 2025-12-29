package com.nihal.Music.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpResponse {

    private Long id;
    private  String name;
    private String email;
}
