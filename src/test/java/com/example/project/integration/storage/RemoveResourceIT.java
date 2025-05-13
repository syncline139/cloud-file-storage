package com.example.project.integration.storage;

import com.example.project.config.TestBeans;
import io.minio.ListObjectsArgs;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestBeans.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Tag("Storage")
@Tag("removeResource")
public class RemoveResourceIT extends BaseStorageTest{


    @BeforeEach
    void clearBucketRemoveBucketClearDatabase() {
        clearBucketAndDatabase();
    }

    @Test
    @SneakyThrows
    void shouldDeleteFileSuccessfully() {
        MockHttpSession session = authorizated();
        String fileName = addFileToBucket();

        mockMvc.perform(delete("/api/resource")
                        .param("path", fileName)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(USERNAME)
                .build())).hasSize(0);
    }
    @Test
    @SneakyThrows
    void shouldDeleteFileInDirectorySuccessfully() {
        MockHttpSession session = authorizated();
        String fileName = addDirectoryToBucket();

        mockMvc.perform(delete("/api/resource")
                        .param("path", fileName)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(searchResourceByName(fileName)).isFalse();
    }

    @Test
    @SneakyThrows
    void shouldDeleteDirectorySuccessfully() {
        MockHttpSession session = authorizated();
        String directory = addDirectoryToBucket();
        String directoryName = directory.substring(0, directory.lastIndexOf("/") + 1);
        mockMvc.perform(delete("/api/resource")
                        .param("path", directoryName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isNoContent());

        assertThat(searchResourceByName(directoryName)).isFalse();
    }


    @Test
    @SneakyThrows
    void shouldFailWhenResourceNotFound() {
        MockHttpSession session = authorizated();
        String fileName = "wallpapers/11.png";

        mockMvc.perform(delete("/api/resource")
                        .param("path", fileName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ресурс не найден"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));

    }

    @SneakyThrows
    @Test
    void shouldFailWithUnauthorizedAccess() {
        String fileName = "file.txt";

        mockMvc.perform(delete("/api/resource")
                        .param("path", fileName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Пользователь не авторизован"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @SneakyThrows
    void shouldFailWithInvalidOrMissingPath() {
        String fileName = "";
        MockHttpSession session = authorizated();

        mockMvc.perform(delete("/api/resource")
                        .param("path", fileName)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Невалидный или отсутствующий путь"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @SneakyThrows
    void normalizedPathSuccessfully() {
        MockHttpSession session = authorizated();
        String directory = addDirectoryToBucket();
        String fileName = String.format("///   %s ///  ", directory);

        mockMvc.perform(delete("/api/resource")
                        .param("path", fileName)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(searchResourceByName(fileName)).isFalse();
    }


}
