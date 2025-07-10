package com.example.project.services;

import com.example.project.dto.response.ResourceInfoResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface StorageService {

    void removeResource(String path);

    void downloadResource(String path, HttpServletResponse response);

    ResourceInfoResponse resourceInfo(String path);

    ResourceInfoResponse moverOrRename(String from, String to);

    List<ResourceInfoResponse> searchResource(String query);

    List<ResourceInfoResponse> directoryContents(String path);

    Set<ResourceInfoResponse> uploadResource(String path, MultipartFile[] objects);

    ResourceInfoResponse createEmptyFolder(String path);
}
