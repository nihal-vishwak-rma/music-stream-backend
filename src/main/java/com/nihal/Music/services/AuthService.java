package com.nihal.Music.services;

import com.nihal.Music.dtos.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;


public interface AuthService {

    SignUpResponse createUser(SignUpRequest signUpRequest);

    LoginResponse loginUser(LoginRequest loginRequest);

    LoginResponse validateUser(String token);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    ResponseEntity<LoginResponse> handleOauth2LoginReqest(OAuth2User oAuth2User, String registrationID);

}
