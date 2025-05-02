package com.example.project.controllers;

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

import java.util.List;
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

    @GetMapping("/download")
    public ResponseEntity<HttpStatus> downloadResource(@RequestParam("path") String path, HttpServletResponse response) {

        storageService.downloadResource(path,response);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }

    /**
     *
     * @param from старый путь
     * @param to новый путь
     */
    @GetMapping("/move")
    public ResponseEntity<?> moverRenamerResource(@RequestParam("from") String from, @RequestParam("to") String to) {

        ResourceInfoResponse response = storageService.moverOrRename(from, to);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchResource(@RequestParam("query") String query) {

        List<ResourceInfoResponse> response = storageService.searchResource(query);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


}


