package com.nihal.Music.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🎵 Music App API")
                        .version("1.0")
                        .contact(new Contact().name("Nihal").name("dhimannihal45@gmail.com"))
                        .description("API documentation for Music Project (Users, Playlists, Admin, Auth etc.)"));


    }


}
