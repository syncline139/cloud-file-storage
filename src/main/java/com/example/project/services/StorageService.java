package com.example.project.services;

import com.example.project.dto.response.ResourceInfoResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {

    ResourceInfoResponse resourceInfo(String path);

    void removeResource(String path);

    void downloadResource(String path, HttpServletResponse response);

    ResourceInfoResponse moverOrRename(String from, String to);

    List<ResourceInfoResponse> searchResource(String query);

    ResourceInfoResponse uploadResource(String path,MultipartFile resource);

}
