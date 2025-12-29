package com.nihal.Music.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForgotPasswordResponse {

    private Long id;
    private String name;
    private String email;

}
