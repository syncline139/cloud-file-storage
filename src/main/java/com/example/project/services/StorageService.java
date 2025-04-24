package com.example.project.services;

import com.example.project.dto.response.ResourceInfoResponse;

public interface StorageService {

    ResourceInfoResponse resourceInfo(String path);

    void removeResource(String path);

}
