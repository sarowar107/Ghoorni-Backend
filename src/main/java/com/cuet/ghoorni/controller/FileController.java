package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.Files;
import com.cuet.ghoorni.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<Files> uploadFile(@RequestParam("file") MultipartFile file,
                                            @RequestParam("topic") String topic,
                                            @RequestParam("isPublic") boolean isPublic,
                                            Authentication authentication) throws IOException {
        Files newFile = fileService.storeFile(file, topic, isPublic, authentication.getName());
        return new ResponseEntity<>(newFile, HttpStatus.CREATED);
    }
    
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {
        Resource file = fileService.loadFileAsResource(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}