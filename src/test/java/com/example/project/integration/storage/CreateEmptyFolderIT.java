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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestBeans.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("Storage")
@Tag("createEmptyFolder")
public class CreateEmptyFolderIT extends BaseStorageTest {

    @BeforeEach
    void clearBucketRemoveBucketClearDatabase() {
        clearBucketAndDatabase();
    }

    @Test
    @SneakyThrows
    void успешноеСозданиеНовойПапкиВБакете() {
        MockHttpSession session = authorizated();

        mockMvc.perform(post("/api/directory")
                        .param("path", "newEmptyFolder")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value(""))
                .andExpect(jsonPath("$.name").value("newEmptyFolder"))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }
    @Test
    @SneakyThrows
    void успешноеСозданиеНовойПапкиВУжеСуществующейДиректории() {
        MockHttpSession session = authorizated();

        String folderObject = "eblan/";
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(USERNAME)
                .object(folderObject)
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .contentType("application/octet-stream")
                .build());

        String pathNewEmptyDirectory = folderObject + "newEmptyFolder/";

        mockMvc.perform(post("/api/directory")
                        .param("path", pathNewEmptyDirectory)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value(folderObject))
                .andExpect(jsonPath("$.name").value("newEmptyFolder"))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }


    @Test
    @SneakyThrows
    void невалидныйИлиОтсутствующийПутьКновойПапке() {
        MockHttpSession session = authorizated();

        mockMvc.perform(post("/api/directory")
                        .param("path", "")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Невалидный или отсутсвующий путь"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));


    }

    @Test
    @SneakyThrows
    void родительскаяПапкаНеСуществует() {
        MockHttpSession session = authorizated();

        mockMvc.perform(post("/api/directory")
                        .param("path", " ffaf/afadfsf")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Родительская папка не существует"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @SneakyThrows
    void папкаСТакимЖеНазваниемУжеСуществует() {
    }


    @Test
    @SneakyThrows
    void пользоватльНеАвторизирован() {
        mockMvc.perform(post("/api/directory").param("path",""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Пользователь не авторизован"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

}
