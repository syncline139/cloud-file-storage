package com.example.project.integration.storage;

import com.example.project.config.TestBeans;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import lombok.SneakyThrows;
import lombok.ToString;
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

import java.io.ByteArrayInputStream;

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

    @BeforeEach
    void clearBucketRemoveBucketClearDatabase() {
        clearBucketAndDatabase();
    }

    @Test
    @SneakyThrows
    void shouldRenameFileSuccessfully() {
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
    void shouldRenameFileInDirectorySuccessfully() {
        MockHttpSession session = authorizated();
        String oldFilePath = addDirectoryToBucket();
        String newFilePath = oldFilePath.substring(0, oldFilePath.lastIndexOf("/") + 1) + "newNameTest.txt";

        mockMvc.perform(get("/api/resource/move")
                        .param("from", oldFilePath)
                        .param("to", newFilePath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(newFilePath.substring(0, newFilePath.lastIndexOf("/")  + 1 )))
                .andExpect(jsonPath("$.name").value(newFilePath.substring(newFilePath.lastIndexOf('/') + 1)))
                .andExpect(jsonPath("$.size").value(sizeInfo(newFilePath)))
                .andExpect(jsonPath("$.type").value("FILE"));
    }

    @Test
    @SneakyThrows
    void shouldMoveFileToDirectorySuccessfully() {
        MockHttpSession session = authorizated();
        String oldFilePath = addFileToBucket();
        String directoryPath = addDirectoryToBucket();
        String directoryName = directoryNameFromFullPathFile(directoryPath);
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
    void shouldRenameDirectorySuccessfully() {
        MockHttpSession session = authorizated();

        String fileFullPathInDirectory = addDirectoryToBucket();
        String oldDirecotryName = fileFullPathInDirectory.substring(0, fileFullPathInDirectory.lastIndexOf("/") + 1);
        String newDirectiryPath = "changed";
        mockMvc.perform(get("/api/resource/move")
                .param("from", oldDirecotryName)
                .param("to", newDirectiryPath)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(""))
                .andExpect(jsonPath("$.name").value(newDirectiryPath))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }

    @Test
    @SneakyThrows
    void shouldMoveDirectorySuccessfully() {
        MockHttpSession session = authorizated();

        String fileFullPathInDirectory_1 = addDirectoryToBucket();
        String fileFullPathInDirectory_2= addDirectoryToBucket();

        String directoryName_1 = directoryNameFromFullPathFile(fileFullPathInDirectory_1);
        String directoryName_2 = directoryNameFromFullPathFile(fileFullPathInDirectory_2);

        mockMvc.perform(get("/api/resource/move")
                        .param("from",directoryName_1)
                        .param("to", directoryName_2 + directoryName_1)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(directoryName_2))
                .andExpect(jsonPath("$.name").value(directoryName_1.substring(0,directoryName_1.lastIndexOf('/'))))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }

    @Test
    @SneakyThrows
    void shouldFailMoveWhenUserNotAuthorized() {


        mockMvc.perform(get("/api/resource/move")
                        .param("from", "")
                        .param("to", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Пользователь не авторизован"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @SneakyThrows
    void shouldFailMoveWithInvalidOrMissingPath() {
        MockHttpSession session = authorizated();

        mockMvc.perform(get("/api/resource/move")
                        .param("from", "")
                        .param("to", "dima/dasd")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Невалидный или отсутствующий путь"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));

    }

    @Test
    @SneakyThrows
    void shouldFailMoveWhenResourceNotFound() {
        MockHttpSession session = authorizated();

        mockMvc.perform(get("/api/resource/move")
                        .param("from", "dfnjkl")
                        .param("to", "dfnjkl")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ресурс не найден"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @SneakyThrows
    void shouldFailMoveWhenFileAlreadyExists() {
        MockHttpSession session = authorizated();

        String fileFullPathInDirectory = addDirectoryToBucket();

        mockMvc.perform(get("/api/resource/move")
                        .param("from", fileFullPathInDirectory)
                        .param("to", fileFullPathInDirectory)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(String.format("Ресурс, лежащий по пути '%s' уже существует",fileFullPathInDirectory)))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CONFLICT.value()));
    }
    @Test
    @SneakyThrows
    void shouldFailMoveWhenDirectoryAlreadyExists() {
        MockHttpSession session = authorizated();

        String fileFullPathInDirectory = addDirectoryToBucket();
        String directoryName = directoryNameFromFullPathFile(fileFullPathInDirectory);

        mockMvc.perform(get("/api/resource/move")
                        .param("from", directoryName)
                        .param("to", directoryName)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(String.format("Ресурс, лежащий по пути '%s' уже существует",
                                directoryName.substring(0,directoryName.lastIndexOf('/')))))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CONFLICT.value()));
    }


}
