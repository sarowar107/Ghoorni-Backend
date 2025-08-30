package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.payload.UserResponse;
import com.cuet.ghoorni.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Get all users (admin only)
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String userId = authentication.getName();
        try {
            // Check if the requesting user is an admin
            if (!adminService.isAdmin(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Admin role required");
            }

            List<User> users = adminService.getAllUsers();
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok().body(userResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users: " + e.getMessage());
        }
    }

    // Delete a user (admin only)
    @DeleteMapping("/users/{targetUserId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable String targetUserId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String adminId = authentication.getName();
        try {
            // Check if the requesting user is an admin
            if (!adminService.isAdmin(adminId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Admin role required");
            }

            adminService.deleteUser(targetUserId);
            return ResponseEntity.ok().body("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user: " + e.getMessage());
        }
    }
}
