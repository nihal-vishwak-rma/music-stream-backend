package com.nihal.Music.dtos;


import com.nihal.Music.entity.role.Roles;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

    private Long id;
    private String email;
    private String token;
    private String refreshToken;
    private Roles role;

}
