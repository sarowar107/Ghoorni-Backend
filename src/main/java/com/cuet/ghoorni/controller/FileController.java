package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.Files;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.payload.FileResponse;
import com.cuet.ghoorni.service.FileService;
import com.cuet.ghoorni.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

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
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("topic") String topic,
            @RequestParam("category") String category,
            @RequestParam("isPublic") boolean isPublic,
            @RequestParam(value = "toDept", required = false) String toDept,
            @RequestParam(value = "toBatch", required = false) String toBatch,
            Authentication authentication) throws IOException {

        // Check if user's email is verified
        User user = userRepository.findByUserId(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmailVerified() == null || !user.getEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Email verification required to upload files");
        }

        Files newFile = fileService.storeFile(file, topic, category, isPublic, authentication.getName(), toDept,
                toBatch);
        return new ResponseEntity<>(FileResponse.fromEntity(newFile), HttpStatus.CREATED);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {
        Resource file = fileService.loadFileAsResource(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    // Get all files with filtering
    @GetMapping
    public ResponseEntity<List<FileResponse>> getAllFiles(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Files> files = fileService.getAllFiles(authentication.getName());
        List<FileResponse> responses = files.stream()
                .map(FileResponse::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}