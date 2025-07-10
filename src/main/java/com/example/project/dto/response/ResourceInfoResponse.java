package com.example.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceInfoResponse(String path, String name, Long size, String type) {

    public static ResourceInfoResponse forFile(String path,
                                               String name,
                                               Long size) {
        return new ResourceInfoResponse(path, name, size, "FILE");
    }

    public static ResourceInfoResponse forDirectory(String path,
                                                    String name) {
        return new ResourceInfoResponse(path, name, null, "DIRECTORY");
    }
}
