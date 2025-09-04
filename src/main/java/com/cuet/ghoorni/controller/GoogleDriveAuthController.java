package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.service.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth/google")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174", "https://ghoorni.netlify.app" })
public class GoogleDriveAuthController {

    @Autowired
    private GoogleDriveService googleDriveService;

    @GetMapping("/authorize")
    public ResponseEntity<?> authorize() {
        try {
            String authUrl = googleDriveService.getAuthorizationUrl();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, authUrl)
                    .build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating authorization URL: " + e.getMessage());
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestParam("code") String code) {
        try {
            googleDriveService.handleAuthorizationCallback(code);
            return ResponseEntity.ok()
                    .body("Google Drive authorization successful! You can now upload files.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error handling authorization callback: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus() {
        try {
            boolean isAuthenticated = googleDriveService.isAuthenticated();
            if (isAuthenticated) {
                return ResponseEntity.ok().body("Google Drive is authenticated and ready for file uploads.");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Google Drive authentication required. Visit /auth/google/authorize to authenticate.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking authentication status: " + e.getMessage());
        }
    }
}
