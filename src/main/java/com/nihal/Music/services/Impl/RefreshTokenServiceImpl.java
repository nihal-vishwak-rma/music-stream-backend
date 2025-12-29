package com.nihal.Music.services.Impl;

import com.nihal.Music.entity.RefreshToken;
import com.nihal.Music.entity.User;
import com.nihal.Music.repositories.RefreshTokenRepository;
import com.nihal.Music.repositories.UserRepository;
import com.nihal.Music.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    long refreshTokenTime = 5 * 60 * 60 * 1000;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public RefreshToken createRefreshToken(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        RefreshToken refreshToken = user.getRefreshToken();

        if (refreshToken == null) {

            refreshToken = new RefreshToken();
            refreshToken.setUser(user);

        }

        refreshToken.setRefreshToken(UUID.randomUUID().toString());
        refreshToken.setExpiry(Instant.now().plusMillis(refreshTokenTime));


        user.setRefreshToken(refreshToken);

        refreshTokenRepository.save(refreshToken);


        return refreshToken;
    }

    @Override
    public RefreshToken verifyRefreshToken(String refreshToken) {

        RefreshToken isvalidToken = refreshTokenRepository.findByRefreshToken(refreshToken);

        if (isvalidToken.getExpiry().compareTo(Instant.now()) < 0) {
            throw new RuntimeException("Refresh token expired");
        }

        return isvalidToken;
    }

}
