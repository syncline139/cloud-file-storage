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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class StorageServiceImpl implements StorageService {

    private final MinioClient minioClient;

    @Override
    public ResourceInfoResponse resourceInfo(String path){
        log.info("Вход в 'resourceInfo', путь {}", path);
        String bucketName = bucketExists();
        String normalizedPath = normalizedPath(path);
        validatePath(path, bucketName);
        return isDirectoryOrFile(normalizedPath, bucketName);
    }
    @Override
    public void removeResource(String path) {
        log.info("Вход в 'removeResource', путь: {}", path);
        String bucketName = bucketExists();
        validatePath(path, bucketName);
        String normalizedPath = normalizedPath(path);
        validatePath(path,bucketName);
        ResourceInfoResponse resourceInfo = isDirectoryOrFile(normalizedPath, bucketName);

        if (!resourceInfo.name().endsWith("/")) {
            deleteFile(normalizedPath, bucketName);
        } else {
            deleteDirectory(normalizedPath, bucketName);
        }

    }

    @Override
    public void downloadResource(String path, HttpServletResponse response) {
        log.info("Вход в метод 'downloadResource', путь: {}", path);
        String bucketName = bucketExists();

        if (path == null || path.trim().isEmpty()) {
            throw new PathNotFoundException("Невозможно скачать бакет");
        }

        String normalizedPath = normalizedPath(path);
        ResourceInfoResponse resourceInfo = isDirectoryOrFile(normalizedPath, bucketName);

        if (resourceInfo.name().endsWith("/")) {
            downloadDirectory(normalizedPath,bucketName, response);
        } else {
            downloadFile(normalizedPath,bucketName, response);
        }
    }

    @Override
    public ResourceInfoResponse moverOrRename(String oldPath, String newPath) {
        log.info("Вошел в метод 'moverOrRename', старый путь: '{}', новый путь: '{}'", oldPath, newPath);

        String bucketName = bucketExists();

        String normalizedOldPath = normalizedPath(oldPath);
        String normalizedNewPath = normalizedPath(newPath);

        if (oldPath.isEmpty() || newPath.isEmpty()) {
            throw new MissingOrInvalidPathException("Невалидный или отсутствующий путь");
        }
        if (isResourceLocked(normalizedNewPath, bucketName)) {
            throw new ResourceAlreadyExistsException(normalizedNewPath);
        }
        ResourceInfoResponse directoryOrFile = isDirectoryOrFile(normalizedOldPath, bucketName);


        if (!directoryOrFile.name().endsWith("/")) {
            renameOrMoveFile(normalizedNewPath, normalizedOldPath, bucketName);
            String nameFile = normalizedNewPath.substring(normalizedNewPath.lastIndexOf('/') + 1);
            String pathToFile = normalizedOldPath.lastIndexOf('/') >= 0
                    ? normalizedOldPath.substring(0, normalizedOldPath.lastIndexOf('/') + 1)
                    : "";
            Long size = directoryOrFile.size();
            return ResourceInfoResponse.forFile(pathToFile,nameFile,size);
        }

        if (directoryOrFile.name().endsWith("/")) {
            renameOrMoveDirectory(normalizedNewPath, normalizedOldPath, bucketName);
            String path;
            String name;

            String cleanPath = normalizedNewPath.endsWith("/")
                    ? normalizedNewPath.substring(0, normalizedNewPath.length() - 1)
                    : normalizedNewPath;

            int lastSlashIndex = cleanPath.lastIndexOf('/');
            if (lastSlashIndex == -1) {
                path = "";
                name = cleanPath;
            } else {
                path = cleanPath.substring(0, lastSlashIndex) + '/';
                name = cleanPath.substring(lastSlashIndex + 1);
            }

            return ResourceInfoResponse.forDirectory(path, name);
        }

        log.warn("Невалидный или отсутствующий путь {}, {}", oldPath, newPath);
        throw new MissingOrInvalidPathException("Невалидный или отсутствующий путь");
    }

    @Override
    public List<ResourceInfoResponse> searchResource(String query) {
        // todo | плохо ищет директорию
        log.info("Вошли в метод 'searchResource'");
        String bucketName = bucketExists();

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
            String name = trimmed.substring(trimmed.lastIndexOf('/') + 1) + "/";
            responseList.add(ResourceInfoResponse.forDirectory(fullPath.substring(0,fullPath.lastIndexOf("/")), name));
        }
        for (Map.Entry<String, Item> entry : files.entrySet()) {
            Item item = entry.getValue();
            String fullPath = item.objectName();
            String name = item.objectName().substring(item.objectName().lastIndexOf('/') + 1);
            Long size = item.size();
            String path = fullPath.substring(0,fullPath.lastIndexOf("/") + 1);
            responseList.add(ResourceInfoResponse.forFile(path, name,size));
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
        String bucketName = bucketExists();

        String normalizedPath = normalizedPath(path);
        if (!normalizedPath.isEmpty() && !normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        return uploadResourceToBucket(bucketName,normalizedPath,objects);
    }

    @Override
    public List<ResourceInfoResponse> directoryContents(String path) {
        log.info("Вход в метод 'directoryContents', путь: {}", path);
        String bucketName = bucketExists();

        String normalizedPath = normalizedPath(path);
        log.debug("Нормализованный путь: {}", normalizedPath);
        boolean pathFile = false;
        try {
            if (!normalizedPath.isEmpty()) {
                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(normalizedPath)
                        .build());
                log.error("Путь '{}' является файлом", path);
                pathFile = true;
            }
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
        boolean found = normalizedPath.isEmpty();
        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                String objectName = item.objectName();

                if (!found && !normalizedPath.isEmpty()) {
                    found = objectName.startsWith(normalizedPath + "/");
                }

                String parentPath = normalizedPath.isEmpty() ? "" : normalizedPath + "/";

                if (objectName.endsWith("/")) {
                    String name = objectName.substring(parentPath.length());
                    if (!name.isEmpty()) {
                        infoResponseList.add(ResourceInfoResponse.forDirectory(parentPath, name));
                    }
                } else {
                    String name = objectName.substring(parentPath.length());
                    infoResponseList.add(ResourceInfoResponse.forFile(parentPath, name, item.size()));
                }

            } catch (Exception e) {
                log.error("Ошибка при обработке объекта: {}", e.getMessage());
            }
        }

        if (pathFile) {
            throw new MissingOrInvalidPathException("Невалидный или отсутствующий путь");
        }

        if (!found) {
            throw new PathNotFoundException("Папка не существует");
        }

        log.debug("Результат: {}", infoResponseList);
        return infoResponseList;
    }
    @Override
    public ResourceInfoResponse createEmptyFolder(String path) {
        // todo допилить эту хрень, она плохо работает.
        String bucketName = bucketExists();
        String normalizedPath = normalizedPath(path);

        // Проверяем, существует ли папка
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(normalizedPath + "/")
                    .recursive(false)
                    .build());

            for (Result<Item> itemResult : results) {
                Item item = itemResult.get();
                if (item.objectName().equals(normalizedPath + "/")) {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            throw new ResourceAlreadyExistsException(normalizedPath);
        }

        // Проверяем, что родительская папка существует в бакете
        String parentFolder = path.contains("/")
                ? normalizedPath.substring(0, normalizedPath.lastIndexOf("/") + 1)
                : "";

        if (!parentFolder.isEmpty()) {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(false)
                    .prefix(parentFolder)
                    .build());

            boolean parentExists = false;
            for (Result<Item> r : results) {
                Item item;
                try {
                    item = r.get();
                } catch (Exception e) {
                    continue;
                }

                if (item.objectName().equals(parentFolder)) {
                    parentExists = true;
                    break; // Если нашли, выходим из цикла
                }
            }

            if (!parentExists) {
                throw new PathNotFoundException("Родительская папка не существует");
            }
        }

        // Папка уже существует
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .recursive(false)
                .prefix(normalizedPath + "/")
                .build());

        for (Result<Item> r : results) {
            Item item;
            try {
                item = r.get();
            } catch (Exception e) {
                continue;
            }
            if (item.objectName().equals(normalizedPath + "/")) {
                throw new ResourceAlreadyExistsException(normalizedPath);
            }
        }
        if (path.isEmpty()) {
            throw new MissingOrInvalidPathException("Невалидный или отсутсвующий путь");
        }

        // Создаём пустую папку
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizedPath + "/")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при создании папки: " + e.getMessage(), e);
        }

        String finalPath = normalizedPath.isEmpty() ? "" : normalizedPath.substring(0, normalizedPath.lastIndexOf("/") + 1);
        String name = normalizedPath.isEmpty() ? "" : normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        return ResourceInfoResponse.forDirectory(finalPath, name);
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

    private String bucketExists() {
        String bucketName = activeUserName();
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
        return bucketName;
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

    private Iterable<Result<Item>> listObjectsRecursiveFalse(String bucketName, String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(false)
                        .build()
        );
    }

    private Iterable<Result<Item>> listObjectsRecursiveTrue(String bucketName) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .recursive(true)
                .build());
    }

    private ResourceInfoResponse isDirectoryOrFile(String normalizedPath, String bucketName) {
        try {
            StatObjectResponse object = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizedPath)
                    .build());
            String name = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);
            String parentPath = parentPathByFullPath(normalizedPath);
                log.info("Путь {} был определён как файл", normalizedPath);
                return ResourceInfoResponse.forFile(parentPath, name, object.size());
        } catch (Exception e) {
            //
        }
        if (directoryExists(bucketName,normalizedPath)) {
            String name = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1)  + "/";
            String parentPath = parentPathByFullPath(normalizedPath);
            log.info("Путь {} был определён как папка", normalizedPath);
            return ResourceInfoResponse.forDirectory(parentPath, name);
        }

        log.error("По пути {} ничего не было найдено", normalizedPath);
        throw new PathNotFoundException("Ресурс не найден");
    }

    private String parentPathByFullPath(String normalizedPath) {
        return normalizedPath.contains("/")
                ? normalizedPath.substring(0, normalizedPath.lastIndexOf("/") + 1)
                : "";
    }

    private boolean directoryExists(String bucketName, String path) {
        Iterable<Result<Item>> results = listObjectsRecursiveFalse(bucketName, path);
        for (Result<Item> r : results) {
            Item item;
            try {
                item = r.get();
            } catch (Exception e) {
                continue;
            }
            if (item.isDir()) {
                return true;
            }
        }
        return false;
    }

    private void validatePath(String path, String bucketName) {
        if (path == null || path.trim().isEmpty()) {
            log.warn("Путь: {} это бакет",bucketName);
            throw new MissingOrInvalidPathException("Невалидный или отсутствующий путь"); // 400
        }
    }

    private void deleteFile(String path, String bucketName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (Exception e) {
            log.info("Во время удаление файла произошла ошибка");
        }
    }

    private void deleteDirectory(String normalizedPath, String bucketName) {
        Iterable<Result<Item>> listObjects =  minioClient.listObjects(ListObjectsArgs.builder()
                .prefix(normalizedPath + "/")
                .bucket(bucketName)
                .recursive(true)
                .build());

        if (!listObjects.iterator().hasNext()) {
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
    }

    private void downloadFile(String normalizedPath, String bucketName,HttpServletResponse response ) {
        try {
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
        }catch (Exception e) {
            log.error("Ошибка при скачивании ресурса по пути {}: {}", normalizedPath, e.getMessage());
            throw new MinioNotFoundException("Ошибка при скачивании из MinIO: " + e.getMessage());
        }
    }

    private void downloadDirectory(String normalizedPath, String bucketName,HttpServletResponse response) {
        try {
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
        } catch (Exception e) {
            log.error("Ошибка при скачивании ресурса по пути {}: {}", normalizedPath, e.getMessage());
            throw new MinioNotFoundException("Ошибка при скачивании из MinIO: " + e.getMessage());
        }
    }



    private void renameOrMoveFile(String normalizedNewPath, String normalizedOldPath, String bucketName) {
        try {
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
        } catch (Exception e) {
            throw new MinioNotFoundException(String.format("Во время переименования файла произошла ошибка: %s", e.getMessage()));
        }
    }

    private void renameOrMoveDirectory(String normalizedNewPath, String normalizedOldPath, String bucketName) {
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
                    String relativePath = item.objectName().substring(normalizedOldPath.length() + 1);
                    String newObjectPath = normalizedNewPath + '/' + relativePath;

                    minioClient.copyObject(CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newObjectPath)
                            .source(CopySource.builder()
                                    .bucket(bucketName)
                                    .object(item.objectName())
                                    .build())
                            .build());
                    log.info("Объект успешно скопирован: {}", newObjectPath);

                    minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(item.objectName())
                            .build());
                    log.info("Старый объект успешно удален: {}", item.objectName());
            } catch (Exception e) {
                throw new MinioNotFoundException(String.format("Ошибка при обработке директории: %s", e.getMessage()));
            }
        }
    }

    private boolean isResourceLocked(String normalizedNewPath, String bucketName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizedNewPath)
                    .build());
            return true;
        }catch (Exception e) {
            log.info("Провальная проверка на файл, скорее всего это директория!");
        }
        String prefix = normalizedNewPath.endsWith("/") ? normalizedNewPath : normalizedNewPath + "/";

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .maxKeys(1)
                .build());

        if (results.iterator().hasNext()) {
            return true;
        }
        return false;
    }

    private Set<ResourceInfoResponse> uploadResourceToBucket(String bucketName, String normalizedPath, MultipartFile[] objects) {

        Set<ResourceInfoResponse> responses = new HashSet<>();

        for (MultipartFile file : objects) {
            String fullPath = normalizedPath + file.getOriginalFilename();
            try {
                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fullPath)
                        .build());
                throw new ResourceAlreadyExistsException(fullPath);
            } catch (ResourceAlreadyExistsException e) {
                log.error("Файл '{}' уже сущестует", file.getOriginalFilename());
                throw new ResourceAlreadyExistsException(fullPath);
            } catch (Exception e) {
                //
            }

            try {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fullPath)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
                responses.add(ResourceInfoResponse.forFile(normalizedPath, file.getOriginalFilename(), file.getSize()));
            } catch (Exception e) {
                log.error("Ошибка при загрузке файла в MinIO: {}", e.getMessage());
            }
        }
        return  responses;
    }
}