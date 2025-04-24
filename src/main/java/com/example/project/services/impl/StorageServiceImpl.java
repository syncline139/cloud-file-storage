package com.example.project.services.impl;

import com.example.project.dto.response.ResourceInfoResponse;
import com.example.project.exceptions.auth.AuthenticationCredentialsNotFoundException;
import com.example.project.exceptions.storage.BucketNotFoundException;
import com.example.project.exceptions.storage.MinioNotFoundException;
import com.example.project.exceptions.storage.MissingOrInvalidPathException;
import com.example.project.exceptions.storage.PathNotFoundException;
import com.example.project.services.StorageService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

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
    @Override
    public ResourceInfoResponse resourceInfo(String path){
        // Получаем имя бакета (логин пользователя)
        String bucketName = activeUserName();
        log.info("Проверяем бакет: {}, путь: {}", bucketName,path);

        bucketExists(bucketName);

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

        for (Result<Item> result : results)  {

            Item item;
            try {
                item = result.get();
            } catch (Exception e) {
                log.error("Не удалось получить Item: {}", e.getMessage());
                continue;
            }

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

    /**
     * yзнать является путь директорией или пакой
     * если файл - удаляем
     * если директория - проходимся по всем директории и удаляем все находившиеся в ней файлы
     * сама папка будет удалена автоматически если она будет пустой
     */
    @Override
    public void removeResource(String path) {

        String bucketName = activeUserName();

        bucketExists(bucketName);

        if (path == null || path.trim().isEmpty()) {
            throw new BucketNotFoundException("Невозможно удалить бакет");
        }

        String normalizedPath = path.replaceAll("^/+|/+$", "");

        boolean file = false;
        boolean isFolderOrFileDeleted = false;
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizedPath)
                    .build());

            file = true;

            minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(normalizedPath)
                    .build());
            isFolderOrFileDeleted = true;
        } catch (Exception e) {
            // это папка
        }


        if (!file) {
          Iterable<Result<Item>> listObjects =  minioClient.listObjects(ListObjectsArgs.builder()
                    .prefix(normalizedPath + "/")
                    .bucket(bucketName)
                    .recursive(true)
                    .build());

            Iterator<Result<Item>> iterator = listObjects.iterator();

            if (!iterator.hasNext()) {
                throw new PathNotFoundException("Ресурс не найден");
            }

            for (Result<Item> result : listObjects) {


                Item item;
                try {
                    item = result.get();
                } catch (Exception e) {
                    continue;
                }

                try {
                    minioClient.removeObject(RemoveObjectArgs.builder()
                                    .bucket(bucketName)
                            .object(item.objectName())
                            .build());
                } catch (Exception e) {
                    throw new PathNotFoundException("Ресурс не найден | " + e.getMessage());
                }
            }
            isFolderOrFileDeleted = true;
        }

        if (!isFolderOrFileDeleted) {
            throw new MissingOrInvalidPathException("Невалидный или отсутствующий путь");
        }



    }

    private void bucketExists(String bucketName) {

        try {
            if (minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build())) {
                log.info("Бакет c именем: {} найден", bucketName);
            }
        } catch (Exception e) {
            log.error("Бакета с именем {} не существует!", bucketName);
            throw new BucketNotFoundException("Бакета '" + bucketName + "' не существует");
        }
    }

    private String activeUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName().equals("anonymousUser")) {
            throw new AuthenticationCredentialsNotFoundException();
        }
        return authentication.getName();
    }
}