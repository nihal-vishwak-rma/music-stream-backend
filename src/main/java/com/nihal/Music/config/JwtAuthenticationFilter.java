package com.nihal.Music.config;

import com.nihal.Music.entity.User;
import com.nihal.Music.repositories.UserRepository;
import com.nihal.Music.security.MusicUserDetails;
import com.nihal.Music.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt_token;
        final  String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer")){
            filterChain.doFilter(request , response);
            return;
        }

        jwt_token = authHeader.substring(7);

        try {
            userEmail = jwtUtil.extractUserName(jwt_token);
            String role = jwtUtil.extractRole(jwt_token);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                if (jwtUtil.validateToken(jwt_token, user)) {

                    MusicUserDetails musicUserDetails = new MusicUserDetails(user);

                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(musicUserDetails,
                            null,
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)));

                    SecurityContextHolder.getContext().setAuthentication(token);
                }

            }
        } catch (JwtException e) {
           response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }
        filterChain.doFilter(request,response);

    }
}
