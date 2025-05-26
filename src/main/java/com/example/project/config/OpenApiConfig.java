package com.example.project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Облачное хранилище")
                        .description("API облачного файлового хранилища с интеграцией MinIO, обеспечивающее регистрацию и авторизацию пользователей через Spring Security, управление файлами и папками, хранение сессий в Redis.")
                        .version("1.0")
                );
    }
}