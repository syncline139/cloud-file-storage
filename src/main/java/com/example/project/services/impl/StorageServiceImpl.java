package com.example.project.services.impl;

import com.example.project.dto.response.ResourceInfoResponse;
import com.example.project.exceptions.auth.AuthenticationCredentialsNotFoundException;
import com.example.project.exceptions.storage.BucketNotFoundException;
import com.example.project.exceptions.storage.MinioNotFoundException;
import com.example.project.exceptions.storage.MissingOrInvalidPathException;
import com.example.project.exceptions.storage.PathNotFoundException;
import com.example.project.services.StorageService;
import com.example.project.utils.FileAction;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    @Override
    public void removeResource(String path) {

        String bucketName = activeUserName();

        bucketExists(bucketName);

        if (path == null || path.trim().isEmpty()) {
            log.error("Невозможно удалить бакет");
            throw new BucketNotFoundException("Невозможно удалить бакет");
        }

        String normalizedPath = path.replaceAll("^/+|/+$", "");

        boolean isFile = false;
        boolean isFolderOrFileDeleted = false;
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizedPath)
                    .build());

            isFile = true;
            log.info("Путь {} был определен как файл", normalizedPath);
            minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(normalizedPath)
                    .build());
            isFolderOrFileDeleted = true;
            log.info("Файл по пути {} был успешно удален",normalizedPath);
        } catch (Exception e) {
            log.info("Путь: {} не является файлом, проверяем как папку", normalizedPath);
        }


        if (!isFile) {
          Iterable<Result<Item>> listObjects =  minioClient.listObjects(ListObjectsArgs.builder()
                    .prefix(normalizedPath + "/")
                    .bucket(bucketName)
                    .recursive(true)
                    .build());

            Iterator<Result<Item>> iterator = listObjects.iterator();

            if (!iterator.hasNext()) {
                log.error("Путь: {} ничего не содержит",normalizedPath);
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
                    log.error("Ресурс не найден | {}", e.getMessage());
                    throw new PathNotFoundException("Ресурс не найден | " + e.getMessage());
                }
            }
            isFolderOrFileDeleted = true;
        }

        if (!isFolderOrFileDeleted) {
            log.error("Невалидный или отсутствующий путь | {}", normalizedPath);
            throw new MissingOrInvalidPathException("Невалидный или отсутствующий путь");
        }
    }


    @Override
    public void downloadResource(String path, HttpServletResponse response) {

        String bucketName = activeUserName();

        bucketExists(bucketName);

        if (path == null || path.trim().isEmpty()) {
            throw new PathNotFoundException("Невозможно скачать бакет");
        }

        String normalizedPath = path.replaceAll("^/+|/+$", "");

        boolean isFile = false;
        boolean hasFiles = false;

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizedPath)
                    .build());
            isFile = true;
        } catch (Exception e) {
            log.info("Путь: {} не является файлом, проверяем как папку", normalizedPath);
        }

        if (!isFile) {
            Iterable<Result<Item>> listObjects = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(normalizedPath + "/")
                    .recursive(true)
                    .build());

            for (Result<Item> result : listObjects) {
                Item item;
                try {
                    item = result.get();
                } catch (Exception e) {
                    continue;
                }

                String objectName = item.objectName();
                if (!objectName.endsWith("/")) {
                    hasFiles = true;
                    break;
                }
            }
        }


        if (!isFile && !hasFiles) {
            log.warn("Ресурс не найден");
            throw new PathNotFoundException("ресурс не найден: " + path);
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"archive.zip\"");


        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            if (isFile) {
                try (InputStream fileToZip = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(normalizedPath)
                        .build())) {

                    String fileName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);
                    zip(zipOut, fileToZip, fileName);
                }
            } else {
                Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(normalizedPath + "/")
                        .recursive(true)
                        .build());

                for (Result<Item> result : results) {
                    Item item = result.get();
                    String objectName = item.objectName();
                    if (objectName.endsWith("/")) { // пропускаем папку
                        continue;
                    }

                    try (InputStream fileToZip = minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build())) {
                        String relativePath = objectName.substring(normalizedPath.length() + 1);
                        zip(zipOut, fileToZip, relativePath);

                    }
                }
            }
            zipOut.finish();

            if (isFile) {
                log.info("Файл по пути: '{}' успешно скачен как archive.zip", normalizedPath);
            } else {
                log.info("Директория по пути: '{}' успешно скачена как archive.zip", normalizedPath);

            }
        } catch (Exception e) {
            log.error("Ошибка при скачивании ресурса по пути {}: {}", normalizedPath, e.getMessage());
            throw new MinioNotFoundException("Ошибка при скачивании из MinIO: " + e.getMessage());
        }
    }


    @Override
    public ResourceInfoResponse moverOrRename(String oldPath, String newPath) {

        boolean isMove = false; // переместить
        boolean isRename = false; // переименовать
        boolean isFile = false; // файл
        boolean isDirecorty = false; // папка

        String bucketName = activeUserName();
        bucketExists(bucketName);


        // проверка на файл
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(oldPath)
                    .build());
            isFile = true;
        } catch (Exception e) {
            log.warn("Путь {} не является файлов, проверяем как папку", oldPath);
        }

        Iterable<Result<Item>> listObjects = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(oldPath)
                .recursive(false)
                .build());

        // проверка на папку
        for (Result<Item> result : listObjects) {
            Item item;
            try {
                item = result.get();
            } catch (Exception e) {
                continue;
            }

            String objectName = item.objectName();

            // Если в папке есть хоть один объект который не заканчивается на '/' значит в папке ест файлы
            if (!objectName.endsWith("/")) {
                isDirecorty = true;
            }


            if (!isFile && !isDirecorty) {
                log.warn("Ресурс не найден");
                throw new PathNotFoundException("ресурс не найден: " + oldPath);
            }


            if (isFile) {
                FileAction action = detectFileAction(oldPath, newPath);

                switch (action) {
                    case MOVE -> isMove = true;
                    case RENAME -> isRename = true;
                    case NONE -> throw new PathNotFoundException("Путь не изменился");
                }
            }

            if (isDirecorty) {
                FileAction action = detectFileAction(oldPath, newPath);

                switch (action) {
                    case MOVE -> isMove = true;
                    case RENAME -> isRename = true;
                    case NONE -> throw new PathNotFoundException("Путь не изменился");
                }
            }

            // переименование
            if (isFile && isRename) {

                try {
                    minioClient.copyObject(CopyObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(newPath)
                                    .source(
                                            CopySource.builder()
                                                    .bucket(bucketName)
                                                    .object(oldPath)
                                                    .build())
                            .build());

                    minioClient.removeObject(RemoveObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(oldPath)
                            .build());

                } catch (Exception e) {
                    // todo
                }
            }



        }
        return null;
    }

    private FileAction detectFileAction(String oldPath, String newPath) {
        String oldDir = oldPath.substring(0, oldPath.lastIndexOf('/') + 1);
        String oldName = oldPath.substring(oldPath.lastIndexOf('/') + 1);

        String newDir = newPath.substring(0, newPath.lastIndexOf('/') + 1);
        String newName = newPath.substring(newPath.lastIndexOf('/') + 1);

        if (!oldDir.equals(newDir)) {
            return FileAction.MOVE;
        } else if (!oldName.equals(newName)) {
            return FileAction.RENAME;
        } else {
            return FileAction.NONE;
        }
    }


    private void zip(ZipOutputStream zipOut, InputStream fileToZip, String relativePath) throws IOException {
        ZipEntry zipEntry = new ZipEntry(relativePath);
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fileToZip.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        zipOut.closeEntry();
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
            throw new BucketNotFoundException(String.format("Бакета '%s' не существует", bucketName));
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