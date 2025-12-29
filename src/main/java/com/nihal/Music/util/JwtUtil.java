package com.nihal.Music.util;

import com.nihal.Music.entity.User;
import com.nihal.Music.entity.role.AuthProviderType;
import com.nihal.Music.security.MusicUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secretKey;

    public static Long getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        MusicUserDetails musicUserDetails = (MusicUserDetails) authentication.getPrincipal();

        return musicUserDetails.getUser().getId();
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    public String extractUserName(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).get("user_id", String.class);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("user_id", user.getId().toString())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .signWith(getSignInKey())
                .compact();

    }

    public boolean validateToken(String jwtToken, User user) {
        final String email = extractUserName(jwtToken);

        return (email.equals(user.getEmail()) && !isTokenExpired(jwtToken));
    }

    private boolean isTokenExpired(String jwtToken) {

        return extractAllClaims(jwtToken).getExpiration().before(new Date());
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }


    // for Oauth features
    public AuthProviderType getProvideTypeFromRegistrationId(String registrationId) {

        return switch (registrationId.toLowerCase()) {

            case "google" -> AuthProviderType.GOOGLE;
            case "github" -> AuthProviderType.GITHUB;
            case "facebook" -> AuthProviderType.FACEBOOK;
            default -> throw new IllegalArgumentException("Unsupported oauth2 Provider : " + registrationId);
        };
    }


    // for Oauth features
    public String determineProviderIdFromOauth2User(OAuth2User auth2User, String registationId) {

        String providerId = switch (registationId.toLowerCase()) {

            case "google" -> auth2User.getAttribute("sub");
            case "github" -> auth2User.getAttribute("id").toString();
            default -> {
                log.error("Unsupported Oauth2 Provider : {}", registationId);
                throw new IllegalArgumentException("Unsupported Oauth2 Provider : " + registationId);
            }
        };

        if (providerId == null || providerId.isBlank()) {
            log.error("unable to determine provideId for provider {} ", registationId);
            throw new IllegalArgumentException("Unable to determine  ProviderId for Oauth2 login: ");

        }

        return providerId;
    }


    // for Oauth features
    public String extractUserNameFromOAuth2User(OAuth2User oAuth2User, String registrationId, String email) {

        String name = null;

        switch (registrationId.toLowerCase()) {
            case "google":
                // Google always sends full name
                name = oAuth2User.getAttribute("name");
                break;

            case "github":
                // GitHub me "name" null ho sakta hai
                name = oAuth2User.getAttribute("name");
                if (name == null || name.isBlank()) {
                    name = oAuth2User.getAttribute("login"); // fallback username
                }
                break;

            case "facebook":
                // Facebook me name hamesha hota hai
                name = oAuth2User.getAttribute("name");
                break;

            default:
                // Agar kuch nahi mila to email ka prefix use karlo
                if (email != null && email.contains("@")) {
                    name = email.split("@")[0];
                } else {
                    name = "UnknownUser"; // last fallback
                }
        }

        return name;
    }


}

