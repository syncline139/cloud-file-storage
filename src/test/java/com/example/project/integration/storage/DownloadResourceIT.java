package com.example.project.integration.storage;

import com.example.project.config.TestBeans;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestBeans.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("Storage")
@Tag("downloadResource")
public class DownloadResourceIT extends BaseStorageTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void clearBucketRemoveBucketClearDatabase() {
        clearBucketAndDatabase();
    }

    @Test
    @SneakyThrows
    void успешноеСкачиваниеФайла() {
        MockHttpSession session = authorizated();
        String filePath = addFileToBucket();
        mockMvc.perform(get("/api/resource/download")
                        .param("path", filePath)
                        .session(session)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk());
    }
    @Test
    @SneakyThrows
    void успешноеСкачиваниеДиректории() {
        MockHttpSession session = authorizated();
        String path = addDirectoryToBucket();
        mockMvc.perform(get("/api/resource/download")
                        .param("path", path.substring(0,path.lastIndexOf('/')))
                        .session(session)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void пользовательНеАвторизирован() {
        mockMvc.perform(get("/api/resource/download")
                        .param("path", "")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Пользователь не авторизован"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @SneakyThrows
    void ресурсНеНайден() {
        MockHttpSession session = authorizated();
        String filePath = addFileToBucket();

        mockMvc.perform(get("/api/resource/download")
                        .param("path", filePath + ".")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(String.format("Ресурс не найден: %s", filePath + ".")))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @SneakyThrows
    void невозможноСкачатьБакет() {
        MockHttpSession session = authorizated();
        String path = "";

        mockMvc.perform(get("/api/resource/download")
                        .param("path", path)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Невозможно скачать бакет"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));

    }

}
