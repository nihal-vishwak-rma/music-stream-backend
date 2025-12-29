package com.nihal.Music.services.Impl;

import com.nihal.Music.dtos.*;
import com.nihal.Music.entity.RefreshToken;
import com.nihal.Music.entity.User;
import com.nihal.Music.entity.role.AuthProviderType;
import com.nihal.Music.entity.role.Roles;
import com.nihal.Music.exception.AccessDeniedException;
import com.nihal.Music.exception.OtpException;
import com.nihal.Music.mapper.UserMapper;
import com.nihal.Music.repositories.UserRepository;
import com.nihal.Music.services.AuthService;
import com.nihal.Music.services.OtpService;
import com.nihal.Music.services.RefreshTokenService;
import com.nihal.Music.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMap;


    @Override
    @Transactional
    public SignUpResponse createUser(SignUpRequest signUpRequest) {

        Optional<User> user = userRepository.findByEmail(signUpRequest.getEmail());

        if (user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        if (signUpRequest.getOtp() == null || signUpRequest.getOtp().isBlank()) {
            throw new OtpException("OTP cannot be blank. Please enter a valid OTP.");
        }


        otpService.verifyOtp(signUpRequest.getOtp(), signUpRequest.getEmail());

        User newUser = userMap.signUpRequestToUser(signUpRequest);
        newUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        newUser.setProviderType(AuthProviderType.EMAIL);
        newUser.setRole(Roles.USER);


        User savedUser = userRepository.save(newUser);

        return userMap.userToSignUpResponse(savedUser);

    }


    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User  not exists"));

        if (user.getProviderType() != AuthProviderType.EMAIL) {
            throw new AccessDeniedException("This account is linked with " + user.getProviderType() + ". Please login with OAuth2.");
        }


        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AccessDeniedException("Invalid email and password");
        }

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        // Token generate karo
        String token = jwtUtil.generateAccessToken(user);


        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .token(token)
                .refreshToken(refreshToken.getRefreshToken())
                .role(user.getRole())
                .build();

    }

    @Override
    @Transactional
    public ResponseEntity<LoginResponse> handleOauth2LoginReqest(OAuth2User oAuth2User, String registrationId) {

        AuthProviderType providerType = jwtUtil.getProvideTypeFromRegistrationId(registrationId);

        String providerId = jwtUtil.determineProviderIdFromOauth2User(oAuth2User, registrationId);

        User user = userRepository.findByProviderIdAndProviderType(providerId, providerType).orElse(null);

        String email = oAuth2User.getAttribute("email");

        String name = jwtUtil.extractUserNameFromOAuth2User(oAuth2User, registrationId, email);

        if (user == null) {

            User emailUser = userRepository.findByEmail(email).orElse(null);

            if (emailUser != null) {

                emailUser.setProviderId(providerId);
                emailUser.setProviderType(providerType);
                user = userRepository.save(emailUser);

            } else {

                user = User.builder()
                        .name(name != null ? name : email.split("@")[0])
                        .email(email)
                        .providerType(providerType)
                        .providerId(providerId)
                        .password(null)
                        .role(Roles.USER)
                        .build();

                userRepository.save(user);

            }

        }

        String token = jwtUtil.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

        LoginResponse loginResponse = new LoginResponse(user.getId(), user.getEmail(), token, refreshToken.getRefreshToken(), user.getRole());


        return ResponseEntity.ok(loginResponse);
    }


    @Override
    public LoginResponse validateUser(String token) {

        String id = jwtUtil.extractUserId(token);

        User user = userRepository.findById(Long.parseLong(id)).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!jwtUtil.validateToken(token, user)) {
            return new LoginResponse(user.getId(), user.getEmail(), null, null, user.getRole());
        }

        return new LoginResponse(user.getId(), user.getEmail(), token, user.getRefreshToken().toString(), user.getRole());
    }


    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {

        User user = userRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        otpService.verifyOtp(forgotPasswordRequest.getOtp(), forgotPasswordRequest.getEmail());

        user.setPassword(passwordEncoder.encode(forgotPasswordRequest.getPassword()));

        userRepository.save(user);

        return ForgotPasswordResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }


}
