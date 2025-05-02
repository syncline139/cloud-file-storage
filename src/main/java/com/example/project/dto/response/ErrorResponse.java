package com.example.project.dto.response;

public record ErrorResponse(String message, int statusCode, long timestamp) {}
