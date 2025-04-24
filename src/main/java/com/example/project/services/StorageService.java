package com.example.project.services;

import com.example.project.dto.response.ResourceInfoResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface StorageService {

    ResourceInfoResponse resourceInfo(String path);

    void removeResource(String path);

    void downloadResource(String path, HttpServletResponse response);

    ResourceInfoResponse moverOrRename(String from, String to);

}
