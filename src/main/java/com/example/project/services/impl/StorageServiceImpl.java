package com.example.project.services.impl;

import com.example.project.dto.response.ResourceInfoResponse;
import com.example.project.exceptions.storage.*;
import com.example.project.services.StorageService;
import com.example.project.utils.Resource;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class StorageServiceImpl implements StorageService {

    private final MinioClient minioClient;

    private final MinioHelperService minioHelperService;

    @Override
    public ResourceInfoResponse resourceInfo(String path){
        log.info("Вход в 'resourceInfo', путь {}", path);
        String bucketName = minioHelperService.bucketExists();
        String normalizedPath = minioHelperService.normalizedPath(path);
        minioHelperService.validatePath(path, bucketName);
        return minioHelperService.getResourceMetadata(normalizedPath, bucketName);
    }
    @Override
    public void removeResource(String path) {
        log.info("Вход в 'removeResource', путь: {}", path);
        String bucketName = minioHelperService.bucketExists();
        minioHelperService.validatePath(path, bucketName);
        String normalizedPath = minioHelperService.normalizedPath(path);
        minioHelperService.validatePath(path,bucketName);
        ResourceInfoResponse resourceInfo = minioHelperService.getResourceMetadata(normalizedPath, bucketName);

        if (!resourceInfo.name().endsWith("/")) {
            minioHelperService.deleteFile(normalizedPath, bucketName);
        } else {
            minioHelperService.deleteDirectory(normalizedPath, bucketName);
        }

    }

    @Override
    public void downloadResource(String path, HttpServletResponse response) {
        log.info("Вход в метод 'downloadResource', путь: {}", path);
        String bucketName = minioHelperService.bucketExists();

        if (path == null || path.trim().isEmpty()) {
            throw new PathNotFoundException("Невозможно скачать бакет");
        }

        String normalizedPath = minioHelperService.normalizedPath(path);
        ResourceInfoResponse resourceInfo = minioHelperService.getResourceMetadata(normalizedPath, bucketName);

        if (resourceInfo.name().endsWith("/")) {
            minioHelperService.downloadDirectory(normalizedPath,bucketName, response);
        } else {
            minioHelperService.downloadFile(normalizedPath,bucketName, response);
        }
    }

    @Override
    public ResourceInfoResponse moverOrRename(String oldPath, String newPath) {
        log.info("Вошел в метод 'moverOrRename', старый путь: '{}', новый путь: '{}'", oldPath, newPath);

        String bucketName = minioHelperService.bucketExists();

        String normalizedOldPath = minioHelperService.normalizedPath(oldPath);
        String normalizedNewPath = minioHelperService.normalizedPath(newPath);

        if (oldPath.isEmpty() || newPath.isEmpty()) {
            throw new MissingOrInvalidPathException("Невалидный или отсутствующий путь");
        }
        if (minioHelperService.isResourceLocked(normalizedNewPath, bucketName)) {
            throw new ResourceAlreadyExistsException(normalizedNewPath);
        }
        ResourceInfoResponse directoryOrFile = minioHelperService.getResourceMetadata(normalizedOldPath, bucketName);


        if (!directoryOrFile.name().endsWith("/")) {
            minioHelperService.renameOrMoveFile(normalizedNewPath, normalizedOldPath, bucketName);
            String nameFile = normalizedNewPath.substring(normalizedNewPath.lastIndexOf('/') + 1);
            String pathToFile = normalizedOldPath.lastIndexOf('/') >= 0
                    ? normalizedOldPath.substring(0, normalizedOldPath.lastIndexOf('/') + 1)
                    : "";
            Long size = directoryOrFile.size();
            return ResourceInfoResponse.forFile(pathToFile,nameFile,size);
        }

        if (directoryOrFile.name().endsWith("/")) {
            minioHelperService.renameOrMoveDirectory(normalizedNewPath, normalizedOldPath, bucketName);
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
        String bucketName = minioHelperService.bucketExists();

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
        String bucketName = minioHelperService.bucketExists();

        String normalizedPath = minioHelperService.normalizedPath(path);
        if (!normalizedPath.isEmpty() && !normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        return minioHelperService.uploadResourceToBucket(bucketName,normalizedPath,objects);
    }

    @Override
    public List<ResourceInfoResponse> directoryContents(String path) {
        log.info("Вход в метод 'directoryContents', путь: {}", path);
        String bucketName = minioHelperService.bucketExists();
        String normalizedPath = minioHelperService.normalizedPath(path);

        Resource directoryOrFile = minioHelperService.identifyResourceType(normalizedPath, bucketName);
        Iterable<Result<Item>> results = minioHelperService.listTopLevelOrDirectory(bucketName, normalizedPath);

        return minioHelperService.getFolderContent(normalizedPath, results, directoryOrFile);
    }
    @Override
    public ResourceInfoResponse createEmptyFolder(String path) {
        String bucketName = minioHelperService.bucketExists();
        String normalizedPath = minioHelperService.normalizedPath(path);

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
}