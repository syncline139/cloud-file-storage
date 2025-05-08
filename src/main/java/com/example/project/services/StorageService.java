package com.example.project.services;

import com.example.project.dto.response.ResourceInfoResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface StorageService {

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
    ResourceInfoResponse resourceInfo(String path);

    void removeResource(String path);

    void downloadResource(String path, HttpServletResponse response);

    ResourceInfoResponse moverOrRename(String from, String to);

    List<ResourceInfoResponse> searchResource(String query);

    Set<ResourceInfoResponse> uploadResource(String path, MultipartFile[] objects);

    List<ResourceInfoResponse> directoryContents(String path);

    ResourceInfoResponse createEmptyFolder(String path);
}
