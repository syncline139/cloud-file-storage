package com.example.project.services.impl;

import com.example.project.dto.response.ResourceInfoResponse;
import com.example.project.exceptions.auth.AuthenticationCredentialsNotFoundException;
import com.example.project.exceptions.storage.BucketNotFoundException;
import com.example.project.exceptions.storage.MissingOrInvalidPathException;
import com.example.project.exceptions.storage.PathNotFoundException;
import com.example.project.services.StorageService;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
@Transactional(readOnly = true)
public class StorageServiceImpl implements StorageService {

    private final MinioClient minioClient;


    /**
     * Получение информации о ресурсе
     *<p>
     *     Если путь, указанный пользователем normalizedPath, полностью совпадает с
     *     <br/>
     *     item.objectName() — это файл.
     *     Если среди объектов есть такие, у которых objectName() начинается с normalizedPath + "/" — это папка.
     *</p>
     *
     * @param path путь пришедший от пользотваля
     */
    @SneakyThrows
    @Override
    public ResourceInfoResponse resourceInfo(String path) {
        // Получаем имя бакета (логин пользователя)
        String bucketName = activeUserName();
        log.info("Проверяем бакет: {}, путь: {}", bucketName,path);

        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            log.warn("Бакета с именем {} не существует!", bucketName);
            throw new BucketNotFoundException("Бакета '" + bucketName + "' не существует");
        } else {
            log.info("Бакет c именем: {} найден",bucketName);
        }

        if (path == null || path.trim().isEmpty()) {
            log.info("Путь: {} это бакет",bucketName);
            return new ResourceInfoResponse("", bucketName, "DIRECTORY");
        }

        String normalizedPath = path.replaceAll("^/+|/+$", "");
        log.info("Путь прошел нормализацию: {}  -->  {}",path,normalizedPath);


        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(normalizedPath)
                        .recursive(false)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            String itemPath = item.objectName();

            // файл
            if (itemPath.equals(normalizedPath)) {
                String name = itemPath.substring(itemPath.lastIndexOf("/") + 1);
                String parentPath = itemPath.contains("/")
                        ? itemPath.substring(0, itemPath.lastIndexOf("/") + 1)
                        : "";
                log.info("путь {} был определен как файл",normalizedPath);
                return new ResourceInfoResponse(parentPath, name, item.size(), "FILE");
            }

            // папка
            if (itemPath.startsWith(normalizedPath + "/")) {
                String name = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);
                String parentPath = normalizedPath.contains("/")
                        ? normalizedPath.substring(0, normalizedPath.lastIndexOf("/") + 1)
                        : "";
                log.info("Путь {} был определен как папка", normalizedPath);
                return new ResourceInfoResponse(parentPath, name, "DIRECTORY");
            } else {
                log.warn("По пути {} ничего не было найдено",normalizedPath);
                throw new PathNotFoundException("Ресурс не найден: " + path);
            }
        }

        log.warn("По пути {} ничего не было найдено",normalizedPath);
        throw new MissingOrInvalidPathException("невалидный или отсутствующий путь: " + path);
    }



    private String activeUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName().equals("anonymousUser")) {
            throw new AuthenticationCredentialsNotFoundException();
        }
        return authentication.getName();
    }
}