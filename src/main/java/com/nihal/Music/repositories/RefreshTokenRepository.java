package com.nihal.Music.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nihal.Music.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long>{


    public RefreshToken findByRefreshToken(String refreshToken);

}
