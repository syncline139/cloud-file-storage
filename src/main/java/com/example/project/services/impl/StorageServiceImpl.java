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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class StorageServiceImpl implements StorageService {

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
        if (minioHelperService.doesResourceExist(normalizedNewPath, bucketName)) {
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
        // todo поиск плохо работает, в конце доделать его
        log.info("Вошли в метод 'searchResource'");
        String bucketName = minioHelperService.bucketExists();
        String normalizedQuery = minioHelperService.normalizedPath(query);
        Iterable<Result<Item>> results = minioHelperService.listAllObjects(bucketName);

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
            String parentPath = fullPath.substring(0, fullPath.lastIndexOf("/"));
            String trimmed = fullPath.endsWith("/") ? fullPath.substring(0, fullPath.length() - 1) : fullPath;
            String name = trimmed.substring(trimmed.lastIndexOf('/') + 1) + "/";
            responseList.add(ResourceInfoResponse.forDirectory(parentPath.substring(0, parentPath.lastIndexOf("/") + 1), name));
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

        if (minioHelperService.directoryExists(bucketName, path)) {
            throw new ResourceAlreadyExistsException(normalizedPath);
        }
        String parentFolder = minioHelperService.parentPathByFullPath(normalizedPath);
        if (!parentFolder.isEmpty()) {
            minioHelperService.parentDirectoryExists(parentFolder, bucketName);
        }

        if (minioHelperService.directoryExists(bucketName,normalizedPath)) {
            throw new ResourceAlreadyExistsException(normalizedPath);
        }

        if (path.isEmpty()) {
            throw new MissingOrInvalidPathException("Невалидный или отсутсвующий путь");
        }
        minioHelperService.createDirectory(normalizedPath, bucketName);

        String finalPath = normalizedPath.isEmpty() ? "" : normalizedPath.substring(0, normalizedPath.lastIndexOf("/") + 1);
        String name = normalizedPath.isEmpty() ? "" : normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        return ResourceInfoResponse.forDirectory(finalPath, name);
    }
}