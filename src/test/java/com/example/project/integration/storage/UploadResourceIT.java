package com.example.project.integration.storage;

import com.example.project.config.TestBeans;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestBeans.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("Storage")
@Tag("uploadResource")
public class UploadResourceIT extends BaseStorageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    void успешнаяЗагрузкаФайловВБакет() {
        MockHttpSession session = authorizated();

        MockMultipartFile file1 = new MockMultipartFile(
                "object", "file1.txt", MediaType.TEXT_PLAIN_VALUE, "файл 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "object", "file2.txt", MediaType.TEXT_PLAIN_VALUE, "файл 2".getBytes());

        String path = "";

        mockMvc.perform(multipart("/api/resource")
                        .file(file1)
                        .file(file2)
                        .param("path", path)
                        .session(session))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].path").value(""))
                .andExpect(jsonPath("$[1].path").value(""))
                .andExpect(jsonPath("$[0].name").value("file1.txt"))
                .andExpect(jsonPath("$[1].name").value("file2.txt"))
                .andExpect(jsonPath("$[0].size").value(file1.getSize()))
                .andExpect(jsonPath("$[1].size").value(file2.getSize()))
                .andExpect(jsonPath("$[0].type").value("FILE"))
                .andExpect(jsonPath("$[1].type").value("FILE"));
    }

    @Test
    @SneakyThrows
    void успешнаяЗагрузкаДиректории() {

    }
}
