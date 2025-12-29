package com.nihal.Music.config;

import com.nihal.Music.services.Impl.SimpleDownloader;
import org.modelmapper.ModelMapper;
import org.schabi.newpipe.extractor.NewPipe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    public AppConfig() {
        NewPipe.init(new SimpleDownloader());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
