package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.payload.LoginRequest;
import com.cuet.ghoorni.service.AuthService;
import com.cuet.ghoorni.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            return new ResponseEntity<>("User ID is required for registration.", HttpStatus.BAD_REQUEST);
        }

        try {
            authService.registerUser(user);
            return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during registration. Please try again.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            String userId = loginRequest.getUserId().trim();
            System.out.println("Login attempt for user: " + userId);

            // Check if the user exists before authentication
            User user = authService.getUserByUserId(userId);
            if (user == null) {
                System.out.println("User not found: " + userId);
                return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
            }

            System.out.println("Found user: " + user.getName() + ", UserId: " + user.getUserId());

            @SuppressWarnings("unused")
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userId, loginRequest.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
            String jwtToken = jwtUtil.generateToken(userDetails);
            System.out.println("JWT generated with subject: " + jwtUtil.extractUsername(jwtToken));

            return ResponseEntity.ok(jwtToken);
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        // First check if we have an authentication object
        if (authentication == null) {
            System.out.println("/me endpoint: Authentication object is null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        // Get user ID directly from the Authentication object
        String userId = authentication.getName();
        System.out.println("/me endpoint: Retrieved userId from Authentication: " + userId);

        // Find the user in the database
        User user = authService.getUserByUserId(userId);

        if (user != null) {
            System.out.println("/me endpoint: Found user: " + user.getName() + ", Role: " + user.getRole()
                    + ", UserId: " + user.getUserId());
            // Create a clean response without circular references
            User cleanUser = new User();
            cleanUser.setUserId(user.getUserId());
            cleanUser.setName(user.getName());
            cleanUser.setEmail(user.getEmail());
            cleanUser.setDeptName(user.getDeptName());
            cleanUser.setBatch(user.getBatch());
            cleanUser.setRole(user.getRole());
            // Don't include password
            cleanUser.setPassword(null);

            return ResponseEntity.ok(cleanUser);
        } else {
            System.out.println("/me endpoint: User not found in database for userId: " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}