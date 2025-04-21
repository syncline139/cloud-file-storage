package com.example.project.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${MINIO_URL:http://localhost:9000}")
    public String URL;

    @Value("${MINIO_USER:minioadmin}")
    private String MINIO_USER;

    @Value("${MINIO_PASSWORD:minioadmin}")
    private String MINIO_PASSWORD;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(URL)
                .credentials(MINIO_USER, MINIO_PASSWORD)
                .build();
    }
}
