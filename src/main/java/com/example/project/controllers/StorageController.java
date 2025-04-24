package com.example.project.controllers;

import com.example.project.dto.response.ResourceInfoResponse;
import com.example.project.services.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
public class StorageController {

    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<?> resourceInfo(@RequestParam("path") String path) {


        ResourceInfoResponse response = storageService.resourceInfo(path);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> removeResource(@RequestParam("path") String path) {

        storageService.removeResource(path);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(null);
    }




}


