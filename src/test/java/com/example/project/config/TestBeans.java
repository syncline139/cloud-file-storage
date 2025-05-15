package com.example.project.config;

import io.minio.MinioClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.testcontainers.utility.DockerImageName.parse;

@TestConfiguration
public class TestBeans {

    public static final String POSTGRES_IMAGE = "postgres:17.4";
    public static final String MINIO_IMAGE = "minio/minio:RELEASE.2025-04-22T22-12-26Z-cpuv1";

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(parse(POSTGRES_IMAGE));
    }

    @Bean
    public MinIOContainer minioContainer() {
        return new MinIOContainer(parse(MINIO_IMAGE));
    }

    @Bean
    public MinioClient minioClient(MinIOContainer minioContainer) {
        return MinioClient.builder()
                .endpoint(minioContainer.getS3URL())
                .credentials(minioContainer.getUserName(), minioContainer.getPassword())
                .build();
    }
}