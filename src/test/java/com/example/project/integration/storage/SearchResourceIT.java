package com.example.project.integration.storage;

import com.example.project.config.TestBeans;
import io.minio.PutObjectArgs;
import lombok.SneakyThrows;
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
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Tag("Storage")
@Tag("searchResource")
public class SearchResourceIT extends BaseStorageTest{


    @Test
    @SneakyThrows
    void shouldSearchFileSuccessfully() {
        MockHttpSession session = authorizated();

        String fileFullPathInDirectory = addDirectoryToBucket();


        mockMvc.perform(get("/api/resource/search")
                        .param("query", fileFullPathInDirectory)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value(fileFullPathInDirectory))
                .andExpect(jsonPath("$[0].name").value(fileFullPathInDirectory.substring(fileFullPathInDirectory.lastIndexOf("/") + 1)))
                .andExpect(jsonPath("$[0].size").value(sizeInfo(fileFullPathInDirectory)))
                .andExpect(jsonPath("$[0].type").value("FILE"));
    }

    // todo | после рефакторинга добавить сюда тест поиска директории ( shouldSearchDirectorySuccessfully )

    @Test
    @SneakyThrows
    void shouldFailSearchWithInvalidOrMissingQuery() {
        MockHttpSession session = authorizated();

        mockMvc.perform(get("/api/resource/search")
                        .param("query","Такого ресурса не существует" )
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Невалидный или отсутствующий поисковый запрос"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @SneakyThrows
    void shouldFailSearchWhenUserNotAuthorized() {
        mockMvc.perform(get("/api/resource/search"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Пользователь не авторизован"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

}
