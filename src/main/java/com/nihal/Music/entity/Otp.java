package com.nihal.Music.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @NotBlank(message = "email is required")
    @Column(name = "user_email", nullable = false)
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private Instant expiry;

}
