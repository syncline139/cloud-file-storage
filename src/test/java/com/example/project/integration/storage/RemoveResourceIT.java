package com.example.project.integration.storage;

import com.example.project.config.TestBeans;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestBeans.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Tag("Storage")
@Tag("removeResource")
public class RemoveResourceIT extends BaseStorageTest{


    @BeforeEach
    @SneakyThrows
    void clearBucketRemoveBucketClearDatabase() {
        clearBucketAndDatabase();
    }

    @Test
    @SneakyThrows
    void успешноеУдалениеФайла() {

    }
    @Test
    @SneakyThrows
    void успешноеУдалениеДиреткории() {

    }
    @Test
    @SneakyThrows
    void невалидныйИлиОтсутствующийПуть() {

    }@Test
    @SneakyThrows
    void пользовательНеАвторизирован() {

    }


}
