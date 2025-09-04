package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.Files;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.payload.FileResponse;
import com.cuet.ghoorni.service.FileService;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174", "https://ghoorni.netlify.app" })
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
            Authentication authentication) {

        try {
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
        } catch (IOException e) {
            // Check if it's a Google Drive authentication error
            if (e.getMessage().contains("Google Drive authentication required")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Google Drive authentication required. Please complete OAuth authorization first by visiting /auth/google/authorize");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long fileId) throws IOException {
        try {
            String downloadLink = fileService.getFileDownloadLink(fileId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, downloadLink)
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("File not found: " + e.getMessage());
        }
    }

    @GetMapping("/view/{fileId}")
    public ResponseEntity<?> viewFile(@PathVariable Long fileId) throws IOException {
        try {
            String viewLink = fileService.getFileViewLink(fileId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, viewLink)
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("File not found: " + e.getMessage());
        }
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