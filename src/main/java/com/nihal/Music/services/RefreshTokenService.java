package com.nihal.Music.services;

import com.nihal.Music.entity.RefreshToken;

public interface RefreshTokenService {

    public RefreshToken createRefreshToken(String userEmail);

    public RefreshToken verifyRefreshToken(String refreshToken);

    

}
