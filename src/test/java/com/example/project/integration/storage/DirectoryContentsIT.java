package com.example.project.integration.storage;

import com.example.project.config.TestBeans;
import io.minio.PutObjectArgs;
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

import java.io.ByteArrayInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestBeans.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("Storage")
@Tag("directoryContents")
public class DirectoryContentsIT extends BaseStorageTest {

    @BeforeEach
    void clearBucketRemoveBucketClearDatabase() {
        clearBucketAndDatabase();
    }

    @Test
    @SneakyThrows
    void невалидныйИлиОтсутствующийПуть() { // 400
        MockHttpSession session = authorizated();

        String fullFilePathInDirectory = addDirectoryToBucket();

        mockMvc.perform(get("/api/directory")
                        .param("path", fullFilePathInDirectory)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Невалидный или отсутствующий путь"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));

    }

    @Test
    @SneakyThrows
    void папкиНеСуществует() { //404
        MockHttpSession session = authorizated();

        mockMvc.perform(get("/api/directory")
                        .param("path", "1sdfsad/fsadf")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Папка не существует"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }
    @Test
    @SneakyThrows
    void пользоватльНеАвторизирован() {
        mockMvc.perform(get("/api/directory")
                        .param("path",""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Пользователь не авторизован"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }
}
