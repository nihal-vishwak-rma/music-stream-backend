package com.nihal.Music.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nihal.Music.dtos.LoginResponse;
import com.nihal.Music.services.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {


    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Value("${app.cors.front-end-url}")
    private String frontendUrl;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String registrationId = token.getAuthorizedClientRegistrationId();

        ResponseEntity<LoginResponse> loginResponse = authService.handleOauth2LoginReqest(oAuth2User, registrationId);


        response.sendRedirect(frontendUrl + "/?token=" + loginResponse.getBody().getToken() + "&refreshToken=" + loginResponse.getBody().getRefreshToken());


    }
}
