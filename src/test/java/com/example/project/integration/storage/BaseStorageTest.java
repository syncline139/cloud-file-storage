package com.example.project.integration.storage;

import com.example.project.dto.request.UserDTO;
import com.example.project.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    protected String addFileToBucker() {
        String file = "file-test.txt";
        String content = "hello world";
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(USERNAME)
                .object(file)
                .stream(new ByteArrayInputStream(content.getBytes()), content.length(), -1)
                .contentType("text/plain")
                .build());

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(USERNAME)
                .build());
        assertThat(results).isNotNull();
        return file;
    }
    @SneakyThrows
    protected String addDirectoryToBucket() {
        String file = "file-test.txt";
        String directory = String.format("test/%s", file);
        String content = "hello world";
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(USERNAME)
                .object(directory)
                .stream(new ByteArrayInputStream(content.getBytes()), content.length(), -1)
                .contentType("text/plain")
                .build());

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(USERNAME)
                .build());
        assertThat(results).isNotNull();
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
    }
}
