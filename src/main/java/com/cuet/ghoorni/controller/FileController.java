package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.Files;
import com.cuet.ghoorni.payload.FileResponse;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class FileController {

    @Autowired
    private FileService fileService;

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            fileService.deleteFile(fileId, authentication.getName());
            return ResponseEntity.ok().body("File deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting file: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("topic") String topic,
            @RequestParam("isPublic") boolean isPublic,
            Authentication authentication) throws IOException {
        Files newFile = fileService.storeFile(file, topic, isPublic, authentication.getName());
        return new ResponseEntity<>(FileResponse.fromEntity(newFile), HttpStatus.CREATED);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {
        Resource file = fileService.loadFileAsResource(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    // New method to view all files
    @GetMapping
    public ResponseEntity<List<FileResponse>> getAllFiles() {
        List<Files> files = fileService.getAllFiles();
        List<FileResponse> responses = files.stream()
                .map(FileResponse::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}