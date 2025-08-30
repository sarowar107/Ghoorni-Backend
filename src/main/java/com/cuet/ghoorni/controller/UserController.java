package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.payload.UpdateNameRequest;
import com.cuet.ghoorni.payload.UpdatePasswordRequest;
import com.cuet.ghoorni.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    // Update name
    @PutMapping("/name")
    public ResponseEntity<?> updateName(
            Authentication authentication,
            @RequestBody UpdateNameRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String userId = authentication.getName();
        try {
            User updatedUser = userService.updateName(userId, request.getName());
            return ResponseEntity.ok().body(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Update password
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(
            Authentication authentication,
            @RequestBody UpdatePasswordRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String userId = authentication.getName();
        boolean success = userService.updatePassword(
                userId,
                request.getCurrentPassword(),
                request.getNewPassword());

        if (success) {
            return ResponseEntity.ok().body("Password updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect");
        }
    }

    // Delete account
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String userId = authentication.getName();
        try {
            userService.deleteAccount(userId);
            return ResponseEntity.ok().body("Account deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete account: " + e.getMessage());
        }
    }
}