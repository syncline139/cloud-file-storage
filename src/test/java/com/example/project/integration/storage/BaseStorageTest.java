package com.example.project.integration.storage;

import com.example.project.dto.request.UserDTO;
import com.example.project.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
public abstract class BaseStorageTest {


    public static final String USERNAME = "luntik";
    public static final String PASSWORD = "qwerty";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MinioClient minioClient;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * При регистрации пользователю выдается кука с активной сессией и создается бакет с названием его юзернейма
     */
    @SneakyThrows
    protected MockHttpSession authorizated() {
        UserDTO userDTO = new UserDTO(USERNAME, PASSWORD);

        MvcResult signUpResult = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(userDTO.getUsername()))
                .andReturn();

        MockHttpSession session = (MockHttpSession) signUpResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }
    @SneakyThrows
    protected String addFileToBucket() {
        String file = "file-test.txt";
        String content = "hello world";
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(USERNAME)
                .object(file)
                .stream(new ByteArrayInputStream(content.getBytes()), content.length(), -1)
                .contentType("text/plain")
                .build());

        assertThat(minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(USERNAME)
                .build())).hasSize(1);
        return file;
    }

    protected String addDirectoryToBucket() {
        String fileName = UUID.randomUUID() + ".txt";
        String directoryName = UUID.randomUUID().toString();
        String directory = String.format("%s/%s",directoryName, fileName);
        String content = "hello world";
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(USERNAME)
                    .object(directory)
                    .stream(new ByteArrayInputStream(content.getBytes()), content.length(), -1)
                    .contentType("text/plain")
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(USERNAME)
                .build())).isNotNull();
        return directory;
    }

    @SneakyThrows
    protected void clearBucketAndDatabase() {
        // Очистка бакета
        if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(USERNAME).build())) {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(USERNAME)
                    .recursive(true)
                    .build());
            for (Result<Item> r : results) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(USERNAME)
                        .object(r.get().objectName())
                        .build());
            }
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(USERNAME)
                    .build());
        }

        // Очистка базы данных
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
        assertThat(minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(USERNAME)
                .build())).isFalse();
    }

    @SneakyThrows
    protected boolean searchResourceByName(String resourceName) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(USERNAME)
                .recursive(true)
                .build());

        for (Result<Item> r : results) {
            Item item = r.get();
            if (item.objectName().equals(resourceName)) {
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    protected Long sizeInfo(String filePath) {
        return minioClient.statObject(StatObjectArgs.builder()
                .bucket(USERNAME)
                .object(filePath)
                .build()).size();
    }

    protected String directoryNameFromFullPathFile(String fullPathBeforeFile) {
        return fullPathBeforeFile.substring(0, fullPathBeforeFile.lastIndexOf('/') + 1);
    }
}
