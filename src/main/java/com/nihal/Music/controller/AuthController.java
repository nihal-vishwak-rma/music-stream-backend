package com.nihal.Music.controller;

import com.nihal.Music.dtos.*;
import com.nihal.Music.entity.RefreshToken;
import com.nihal.Music.entity.User;
import com.nihal.Music.repositories.UserRepository;
import com.nihal.Music.services.AuthService;
import com.nihal.Music.services.OtpService;
import com.nihal.Music.services.RefreshTokenService;
import com.nihal.Music.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "APIs for User Authentication (Signup and Login)")
public class AuthController {


    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final OtpService otpService;
    private final UserRepository userRepository;


    @Autowired
    private JwtUtil jwtUtil;


    /**
     * Create a new user account.
     *
     * @param signUpRequest user signup details (username, email, password)
     * @return SignUpResponse with created user information
     */


    @PostMapping("/signup")
    @Operation(
            summary = "Create a new user",
            description = "API to register a new user into the system",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = SignUpResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content),
            }
    )
    public ResponseEntity<SignUpResponse> signupUser(@Valid @RequestBody SignUpRequest signUpRequest) {

        SignUpResponse user = authService.createUser(signUpRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);

    }

    /**
     * Authenticate an existing user and return a JWT token.
     *
     * @param loginRequest user login details (email, password)
     * @return LoginResponse with JWT token and user details
     */

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "API for user authentication. Returns a JWT token if credentials are valid.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content)
            }
    )
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {

        LoginResponse user = authService.loginUser(loginRequest);

        return ResponseEntity.ok(user);

    }

    @GetMapping("/validate")
    public ResponseEntity<com.nihal.Music.dtos.ApiResponse<LoginResponse>> validate(@RequestHeader("Authorization") String token) {


        if (token == null || !token.startsWith("Bearer")) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new com.nihal.Music.dtos.ApiResponse<>(false, null, "Missing or invalid token"));
        }

        String validToken = token.substring(7);

        LoginResponse response = authService.validateUser(validToken);

        if (response.getId() == null || response.getEmail() == null && response.getRole() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new com.nihal.Music.dtos.ApiResponse<>(false, null, "Token invalid or expired"));
        }


        return ResponseEntity.status(HttpStatus.OK).body(new com.nihal.Music.dtos.ApiResponse<>(true, response, response.getId() != null ? "Authenticated" : "Not Authenticated"));
    }


    @PostMapping("/refresh")
    public ResponseEntity<com.nihal.Music.dtos.ApiResponse<LoginResponse>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {

        RefreshToken verifyRefreshToken = refreshTokenService.verifyRefreshToken(refreshTokenRequest.getRefreshtoken());

        User user = verifyRefreshToken.getUser();

        String token = jwtUtil.generateAccessToken(user);

        LoginResponse loginResponse = LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .token(token)
                .refreshToken(verifyRefreshToken.getRefreshToken())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(new com.nihal.Music.dtos.ApiResponse<>(true, loginResponse, "verifed refresh token"));
    }


    @PostMapping("/send-otp")
    public ResponseEntity<com.nihal.Music.dtos.ApiResponse<Object>> sendOtp(@Valid @RequestBody OtpRequest otpRequest) {

        boolean sent = otpService.sendOtp(otpRequest);

        if (sent) {
            return ResponseEntity.ok(new com.nihal.Music.dtos.ApiResponse<>(true, null, "OTP sent successfully."));
        } else {
            return ResponseEntity.badRequest().body(new com.nihal.Music.dtos.ApiResponse<>(false, null, "Failed to send OTP."));
        }
    }

    @PostMapping("/forgot-password-otp")
    public ResponseEntity<com.nihal.Music.dtos.ApiResponse<Object>> forgotPasswordSendOtp(@RequestBody OtpRequest otpRequest) {

        User user = userRepository.findByEmail(otpRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not exits."));

        boolean sent = otpService.sendOtp(otpRequest);

        if (sent) {
            return ResponseEntity.ok(new com.nihal.Music.dtos.ApiResponse<>(true, null, "OTP sent successfully."));
        } else {
            return ResponseEntity.badRequest().body(new com.nihal.Music.dtos.ApiResponse<>(false, null, "Failed to send OTP."));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<com.nihal.Music.dtos.ApiResponse<ForgotPasswordResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {

        ForgotPasswordResponse forgotPasswordResponse = authService.forgotPassword(forgotPasswordRequest);


        return ResponseEntity.status(HttpStatus.OK)
                .body(new com.nihal.Music.dtos.ApiResponse<>(true, forgotPasswordResponse, "Password changed successfully."));
    }


}
