package com.example.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceInfoResponse(String path, String name, Long size, String type) {

    public ResourceInfoResponse(String path, String name, String type) {
        this(path, name, null, type);
    }
}
