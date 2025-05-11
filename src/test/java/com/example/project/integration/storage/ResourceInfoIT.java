package com.example.project.integration.storage;

import com.example.project.config.TestBeans;
import io.minio.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;



import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(classes = TestBeans.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Tag("Storage")
@Tag("resourceInfo")
public class ResourceInfoIT extends BaseStorageTest {

    @BeforeEach
    @SneakyThrows
    void clearBucketRemoveBucketClearDatabase() {
        clearBucketAndDatabase();
    }

    @SneakyThrows
    @Test
    void shouldReturnFileInfoForValidPath() {
        MockHttpSession session = authorizated();

        String file = addFileToBucker();

        mockMvc.perform(get("/api/resource")
                        .param("path", file)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(""))
                .andExpect(jsonPath("$.name").value(file))
                .andExpect(jsonPath("$.size").value(11))
                .andExpect(jsonPath("$.type").value("FILE"));

        assertThat(minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(USERNAME)
                .build())).isTrue();
    }

    @SneakyThrows
    @Test
    void shouldFailWithUnauthorizedAccess() {

        String fileName = "file.txt";

        mockMvc.perform(get("/api/resource")
                        .param("path", fileName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Пользователь не авторизован"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @SneakyThrows
    @Test
    void shouldFailWithResourceNotFound() {
        String fileName = "123.txt";

        mockMvc.perform(get("/api/resource")
                        .param("path", fileName)
                        .session(authorizated())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ресурс не найден"));
    }
    @SneakyThrows
    @Test
    void shouldFailWithInvalidPath() {
        String fileName = " ";

        mockMvc.perform(get("/api/resource")
                        .param("path", fileName)
                        .session(authorizated())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Невалидный или отсутствующий путь"));
    }
    @SneakyThrows
    @Test
    void shouldReturnFileInfoInDirectory() {
        MockHttpSession session = authorizated();

        String directory = addDirectoryToBucket();

        mockMvc.perform(get("/api/resource")
                        .param("path", directory)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(directory.substring(0,directory.lastIndexOf("/") + 1)))
                .andExpect(jsonPath("$.name").value(directory.substring(directory.lastIndexOf("/")  + 1)))
                .andExpect(jsonPath("$.size").value(11))
                .andExpect(jsonPath("$.type").value("FILE"));

        assertThat(minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(USERNAME)
                .build())).isTrue();
    }
    @SneakyThrows
    @Test
    void shouldReturnDirectoryInfo() {
        MockHttpSession session = authorizated();

        String directory = addDirectoryToBucket();

        mockMvc.perform(get("/api/resource")
                        .param("path", directory.substring(0,directory.lastIndexOf("/") + 1))
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(""))
                .andExpect(jsonPath("$.name").value(directory.substring(0,directory.lastIndexOf("/"))))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));

        assertThat(minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(USERNAME)
                .build())).isTrue();
    }
}
