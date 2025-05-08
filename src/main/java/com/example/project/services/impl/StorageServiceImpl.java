package com.example.project.services.impl;

import com.example.project.dto.response.ResourceInfoResponse;
import com.example.project.exceptions.auth.AuthenticationCredentialsNotFoundException;
import com.example.project.exceptions.storage.*;
import com.example.project.services.StorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
@Transactional(readOnly = true)
public class StorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final Object fileSystemOperationLock = new Object();

    @Override
    public ResourceInfoResponse resourceInfo(String path){
        log.info("Вход в 'resourceInfo', путь {}", path);
        String bucketName = activeUserName();

        bucketExists(bucketName);

        if (path == null || path.trim().isEmpty()) {
            log.warn("Путь: {} это бакет",bucketName);
            return new ResourceInfoResponse("", bucketName, "DIRECTORY");
        }

        String normalizedPath = normalizedPath(path);

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(normalizedPath)
                        .recursive(false)
                        .build()
        );

        log.debug("Полученны объекты для пути: {}", normalizedPath);

        for (Result<Item> result : results)  {
            Item item;
            try {
                item = result.get();
            } catch (Exception e) {
                log.error("Ошибка при получении объекта для пути {}: {}", normalizedPath, e.getMessage());
                throw new PathNotFoundException(e.toString()); //404
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
                log.error("По пути {} ничего не было найдено",normalizedPath);
                throw new PathNotFoundException(String.format("Ресурс не найден: '%s'", path)); // 404
            }
        }

        log.error("По пути {} ничего не было найдено",normalizedPath);
        throw new MissingOrInvalidPathException(String.format("Невалидный или отсутствующий путь: '%s'", path)); // 400
    }

    @Override
    public void removeResource(String path) {
        log.info("Вход в 'removeResource', путь: {}", path);
        String bucketName = activeUserName();

        bucketExists(bucketName);

        if (path == null || path.trim().isEmpty()) {
            log.error("Невозможно удалить бакет");
            throw new MissingOrInvalidPathException(String.format("Невалидный или отсутствующий путь: %s", path)); // 400
        }

        String normalizedPath = normalizedPath(path);

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
        log.info("Вход в метод 'downloadResource', путь: {}", path);
        String bucketName = activeUserName();

        bucketExists(bucketName);

        if (path == null || path.trim().isEmpty()) {
            throw new PathNotFoundException("Невозможно скачать бакет");
        }

        String normalizedPath = normalizedPath(path);

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

        try {
            if (isFile) {
                String fileName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);

                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

                try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(normalizedPath)
                        .build());
                     OutputStream out = response.getOutputStream()) {
                    inputStream.transferTo(out);
                }

                log.info("Файл по пути '{}' успешно скачен как '{}'", normalizedPath, fileName);

            } else {
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"archive.zip\"");

                try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
                    Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(normalizedPath + "/")
                            .recursive(true)
                            .build());

                    for (Result<Item> result : results) {
                        Item item = result.get();
                        String objectName = item.objectName();
                        if (objectName.endsWith("/")) {
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
                    zipOut.finish();
                }

                log.info("Директория по пути '{}' успешно скачена как архив", normalizedPath);
            }
        } catch (Exception e) {
            log.error("Ошибка при скачивании ресурса по пути {}: {}", normalizedPath, e.getMessage());
            throw new MinioNotFoundException("Ошибка при скачивании из MinIO: " + e.getMessage());
        }
    }


    @Override
    public ResourceInfoResponse moverOrRename(String oldPath, String newPath) {
        log.info("Вошел в метод 'moverOrRename', старый путь: '{}', новый путь: '{}'", oldPath, newPath);

        boolean isFile = false; // файл
        boolean isDirectory = false; // папка
        long size = 0; // размер файла

        String bucketName = activeUserName();
        bucketExists(bucketName);

        String normalizedOldPath = oldPath.replaceAll("^/+|/+$", "");
        String normalizedNewPath = newPath.replaceAll("^/+|/+$", "");

        // проверка на файл
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizedOldPath)
                    .build());
            isFile = true;
            size = statObjectResponse.size();
            log.info("Путь: '{}' является файлом", normalizedNewPath);
        } catch (Exception e) {
            log.warn("Путь: '{}' не является файлов, проверяем как папку", normalizedOldPath + '/');
        }

        // проверка на папку
        if (!isFile) {
            Iterable<Result<Item>> listObjects = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(normalizedOldPath + '/')
                    .recursive(true)
                    .build());
            for (Result<Item> result : listObjects) {
                Item item;
                try {
                    item = result.get();
                    String objectName = item.objectName();

                    // Если в папке есть хоть один объект который не заканчивается на '/' значит в папке ест файлы
                    if (!objectName.endsWith("/")) {
                        log.info("Путь: {} был определен как дирктория", normalizedNewPath + '/');
                        isDirectory = true;
                    }
                } catch (Exception e) {
                    log.warn("Путь '{}' не является папкой: -> {}",oldPath ,e.getMessage());
                }

            }
        }

        if (!isFile && !isDirectory) {
                log.warn("Ресурс не найден {}",normalizedOldPath);
                throw new PathNotFoundException("ресурс не найден: " + oldPath);
            }

        if (isFile) {
            try {
                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(normalizedNewPath)
                        .build());
                throw new ResourceAlreadyExistsException(normalizedNewPath);
            } catch (ResourceAlreadyExistsException e) {
                log.warn("Файл, лежащий по пути '{}' уже существует",normalizedNewPath);
                throw e;
            }catch (Exception e) {
                log.info("Провальная проверка на файл, скорее всего это директория!");
            }
        }
        if (isDirectory) {
            String prefix = normalizedNewPath.endsWith("/") ? normalizedNewPath : normalizedNewPath + "/";

            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .maxKeys(1)
                    .build());

            if (results.iterator().hasNext()) {
                throw new ResourceAlreadyExistsException(normalizedNewPath);
            }
        }


        // переименование файла
            if (isFile) {
                log.info("Попытка переименовать файл");
                try {
                    synchronized (fileSystemOperationLock) {
                        minioClient.copyObject(CopyObjectArgs.builder()
                                .bucket(bucketName)
                                .object(normalizedNewPath)
                                .source(
                                        CopySource.builder()
                                                .bucket(bucketName)
                                                .object(normalizedOldPath)
                                                .build())
                                .build());
                        log.info("Файл успешно скопирован");

                        minioClient.removeObject(RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(normalizedOldPath)
                                .build());
                        log.info("Старый файл успешно удален");
                        log.info("Переименование файла прошло успешно");
                    }
                } catch (Exception e) {
                    throw new MinioNotFoundException(String.format("Во время переименования файла произошла ошибка: %s", e.getMessage()));
                }
            }

        if (isFile) {
            String nameFile = normalizedNewPath.substring(normalizedNewPath.lastIndexOf('/') + 1);
            return new ResourceInfoResponse(normalizedNewPath,nameFile,size, "FILE");
        }

        if (isDirectory) {
            Iterable<Result<Item>> listObjects = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(normalizedOldPath + '/')
                    .recursive(true)
                    .build());

            for (Result<Item> result : listObjects) {
                Item item;
                try {
                    item = result.get();
                } catch (Exception e) {
                    continue;
                }

                try {
                    synchronized (fileSystemOperationLock) {
                        // Формируем новый путь для объекта
                        String relativePath = item.objectName().substring(normalizedOldPath.length() + 1);
                        String newObjectPath = normalizedNewPath + '/' + relativePath;

                        // Копируем объект в новый путь
                        minioClient.copyObject(CopyObjectArgs.builder()
                                .bucket(bucketName)
                                .object(newObjectPath)
                                .source(CopySource.builder()
                                        .bucket(bucketName)
                                        .object(item.objectName())
                                        .build())
                                .build());
                        log.info("Объект успешно скопирован: {}", newObjectPath);

                        // Удаляем старый объект
                        minioClient.removeObject(RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(item.objectName())
                                .build());
                        log.info("Старый объект успешно удален: {}", item.objectName());
                    }
                } catch (Exception e) {
                    throw new MinioNotFoundException(String.format("Ошибка при обработке директории: %s", e.getMessage()));
                }
            }
        }

        if (isDirectory) {
            String path = normalizedNewPath.substring(0,normalizedNewPath.lastIndexOf("/"));
            String name = normalizedNewPath.substring(normalizedNewPath.lastIndexOf('/'));
            return new ResourceInfoResponse(path,name, "DIRECTORY");
        }
        log.warn("Невалидный или отсутствующий путь {}, {}", oldPath, newPath);
        throw new MissingOrInvalidPathException("Невалидный или отсутствующий путь");
    }

    @Override
    public List<ResourceInfoResponse> searchResource(String query) {
        log.info("Вошли в метод 'searchResource'");
        String bucketName = activeUserName();
        bucketExists(bucketName);

        String normalizedQuery = query.replaceAll("^/+|/+$", "");

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .recursive(true)
                .build());

        HashMap<String, Item> dirs = new HashMap<>();
        HashMap<String, Item> files = new HashMap<>();

        for (Result<Item> r : results) {
            Item item;
            String objectName;
            try {
                item = r.get();
                objectName = item.objectName();
            } catch (Exception e) {
                continue;
            }
            if (objectName.contains(normalizedQuery)) {
                if (objectName.endsWith("/")) {
                    dirs.put(objectName, item);
                } else {
                    files.put(objectName, item);
                }
            }

        }

        List<ResourceInfoResponse> responseList = new ArrayList<>();

        for (Map.Entry<String, Item> entry : dirs.entrySet()) {
            Item item = entry.getValue();
            String fullPath = item.objectName();
            String trimmed = fullPath.endsWith("/") ? fullPath.substring(0, fullPath.length() - 1) : fullPath;
            String name = trimmed.substring(trimmed.lastIndexOf('/') + 1);
            responseList.add(new ResourceInfoResponse(fullPath, name,"DIRECTORY"));
        }
        for (Map.Entry<String, Item> entry : files.entrySet()) {
            Item item = entry.getValue();
            String fullPath = item.objectName();
            String name = item.objectName().substring(item.objectName().lastIndexOf('/') + 1);
            Long size = item.size();
            responseList.add(new ResourceInfoResponse(fullPath, name,size,"FILE"));
        }

        if (responseList.isEmpty()) {
            log.warn("Невалидный или отсутствующий поисковый запрос");
            throw new MissingOrInvalidPathException("Невалидный или отсутствующий поисковый запрос");
        }
        log.info("Ресурс найден");
        return responseList;
    }


    @Override
    public Set<ResourceInfoResponse> uploadResource(String path, MultipartFile[] objects){
        log.info("Вошли в метод 'uploadResource'");
        String bucketName = activeUserName();
        bucketExists(bucketName);

        String normalizedPath = normalizedPath(path);
        if (!normalizedPath.isEmpty() && !normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        Set<ResourceInfoResponse> fina = new HashSet<>();

        for (MultipartFile file : objects) {

            String objectName = normalizedPath + file.getOriginalFilename();

            try {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
                fina.add(new ResourceInfoResponse(normalizedPath, file.getName(), file.getSize(), "FILE"));
            } catch (Exception e) {
                throw new MinioNotFoundException("Ошибка при загрузке файла в MinIO");
            }
        }
        return fina;
    }

    @Override
    public List<ResourceInfoResponse> directoryContents(String path) {
        log.info("Вход в метод 'directoryContents', путь: {}", path);
        String bucketName = activeUserName();
        bucketExists(bucketName);

        String normalizedPath = normalizedPath(path);
        log.debug("Нормализованный путь: {}", normalizedPath);

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizedPath)
                    .build());
            log.error("Путь '{}' является файлом", path);
            throw new MissingOrInvalidPathException("Указанный путь является файлом, ожидается директория");
        } catch (Exception e) {
            log.info("Путь '{}' является директорией или не существует", path);
        }

        Iterable<Result<Item>> results;
        if (normalizedPath.isEmpty()) {
            results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(false)
                    .build());
        } else {
            results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(normalizedPath + "/")
                    .recursive(false)
                    .build());
        }
        List<ResourceInfoResponse> infoResponseList = new ArrayList<>();
        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                String objectName = item.objectName();
                String parentPath = normalizedPath.isEmpty() ? "" : normalizedPath + "/";

                if (objectName.endsWith("/")) {
                    String name = objectName.substring(parentPath.length());
                    if (!name.isEmpty()) {
                        infoResponseList.add(new ResourceInfoResponse(parentPath, name, "DIRECTORY"));
                    }
                } else {
                    String name = objectName.substring(parentPath.length());
                    infoResponseList.add(new ResourceInfoResponse(parentPath, name, item.size(), "FILE"));
                }

            } catch (Exception e) {
                log.error("Ошибка при обработке объекта: {}", e.getMessage());
            }
        }


        log.debug("Результат: {}", infoResponseList);
        return infoResponseList;
    }
    @Override
    public ResourceInfoResponse createEmptyFolder(String path) {

        String bucketName = activeUserName();
        bucketExists(bucketName);

        String normalizdPath = normalizedPath(path);

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizdPath)
                    .build());
            throw new MissingOrInvalidPathException("Невалидный или отсутствующий путь к новой папке");
        } catch (Exception e) {
            //
        }

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizdPath + "/")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            //
        }

        String finalPath = normalizdPath;
        String name = normalizdPath;
        return new ResourceInfoResponse(finalPath,name,"DIRECTORY");
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
        log.info("Проверяем бакет: {}", bucketName);
        try {
            if (minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build())) {
                log.debug("Бакет {} существует", bucketName);
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

    private String normalizedPath(String path) {
        String normalizedPath = path.replaceAll("^/+|/+$", "").trim();
        log.info("Путь прошел нормализацию: {}  -->  {}",path,normalizedPath);
        return !normalizedPath.isEmpty() ? normalizedPath : "";
    }
}