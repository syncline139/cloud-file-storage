package com.example.project.controllers;

import com.example.project.annotations.storage.ResourceInfoDoc;
import com.example.project.dto.response.ResourceInfoResponse;
import com.example.project.services.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StorageController {

    private final StorageService storageService;

    @ResourceInfoDoc
    @GetMapping("/resource")
    public ResponseEntity<?> resourceInfo(@RequestParam("path") String path) {

        ResourceInfoResponse response = storageService.resourceInfo(path);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<HttpStatus> removeResource(@RequestParam("path") String path) {

        storageService.removeResource(path);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(null);
    }


    @PostMapping("/resource")
    public ResponseEntity<?> uploadResource(@RequestParam("path") String path,
                                            @RequestParam("object") MultipartFile[] objects) {

        Set<ResourceInfoResponse> responses = storageService.uploadResource(path, objects);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responses);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<HttpStatus> downloadResource(@RequestParam("path") String path, HttpServletResponse response) {

        storageService.downloadResource(path, response);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }

    /**
     * @param from старый путь
     * @param to   новый путь
     */
    @GetMapping("/resource/move")
    public ResponseEntity<?> moverRenamerResource(@RequestParam("from") String from, @RequestParam("to") String to) {

        ResourceInfoResponse response = storageService.moverOrRename(from, to);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/resource/search")
    public ResponseEntity<?> searchResource(@RequestParam("query") String query) {

        List<ResourceInfoResponse> response = storageService.searchResource(query);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/directory")
    public ResponseEntity<?> directoryContents(@RequestParam("path") String path) {

        List<ResourceInfoResponse> response = storageService.directoryContents(path);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/directory")
    public ResponseEntity<?> createEmptyFolder(@RequestParam("path") String path) {

        ResourceInfoResponse response = storageService.createEmptyFolder(path);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


}


