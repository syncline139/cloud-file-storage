package com.example.project.integration.storage;

import com.example.project.config.TestBeans;
import io.minio.StatObjectArgs;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestBeans.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("Storage")
@Tag("moverOrRename")
public class MoverOrRenameIT extends BaseStorageTest {

    @Autowired
    private MockMvc mockMvc;



    @BeforeEach
    void clearBucketRemoveBucketClearDatabase() {
        clearBucketAndDatabase();
    }

    @Test
    @SneakyThrows
    void успешноеПереименованиеФайла() {
        MockHttpSession session = authorizated();
        String oldFilePath = addFileToBucket();
        String newFilePath = oldFilePath.substring(0, oldFilePath.lastIndexOf("/") + 1) + "newNameTest.txt";

        mockMvc.perform(get("/api/resource/move")
                        .param("from", oldFilePath)
                        .param("to", newFilePath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(""))
                .andExpect(jsonPath("$.name").value(newFilePath.substring(newFilePath.lastIndexOf('/') + 1)))
                .andExpect(jsonPath("$.size").value(sizeInfo(newFilePath)))
                .andExpect(jsonPath("$.type").value("FILE"));
    }
    @Test
    @SneakyThrows
    void успешноеПереименованиеФайлаВДиректории() {
        MockHttpSession session = authorizated();
        String oldFilePath = addDirectoryToBucket();
        String newFilePath = oldFilePath.substring(0, oldFilePath.lastIndexOf("/") + 1) + "newNameTest.txt";

        mockMvc.perform(get("/api/resource/move")
                        .param("from", oldFilePath)
                        .param("to", newFilePath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(oldFilePath.substring(0,oldFilePath.lastIndexOf('/') + 1)))
                .andExpect(jsonPath("$.name").value(newFilePath.substring(newFilePath.lastIndexOf('/') + 1)))
                .andExpect(jsonPath("$.size").value(sizeInfo(newFilePath)))
                .andExpect(jsonPath("$.type").value("FILE"));
    }

    @Test
    @SneakyThrows
    void перемещениеФайла() {
        MockHttpSession session = authorizated();
        String oldFilePath = addFileToBucket();
        String directoryPath = addDirectoryToBucket();
        String directoryName = directoryPath.substring(0, directoryPath.lastIndexOf('/') + 1);
        String newFilePath = directoryName + "newNameFile";
        mockMvc.perform(get("/api/resource/move")
                .param("from", oldFilePath)
                .param("to", newFilePath)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(oldFilePath.substring(0,oldFilePath.lastIndexOf('/') + 1)))
                .andExpect(jsonPath("$.name").value(newFilePath.substring(newFilePath.lastIndexOf('/') + 1)))
                .andExpect(jsonPath("$.size").value(sizeInfo(newFilePath)))
                .andExpect(jsonPath("$.type").value("FILE"));
        assertThat(minioClient.statObject(StatObjectArgs.builder()
                .bucket(USERNAME)
                .object(newFilePath)
                .build())).isNotNull();
    }

    @Test
    @SneakyThrows
    void переменованиеДиректории() {
        MockHttpSession session = authorizated();

        String directoryPath = addDirectoryToBucket();
        String directoryName = directoryPath.substring(0, directoryPath.lastIndexOf('/') + 1);
        String PZIDEC = directoryName.substring(0,directoryName.lastIndexOf('/'));
        mockMvc.perform(get("/api/resource/move")
                .param("from", directoryPath)
                .param("to", directoryName + "test")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(directoryPath.substring(0,directoryPath.lastIndexOf('/') + 1)))
                .andExpect(jsonPath("$.name").value(PZIDEC))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }


}
