package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.model.EmailVerificationToken;
import com.cuet.ghoorni.payload.LoginRequest;
import com.cuet.ghoorni.service.AuthService;
import com.cuet.ghoorni.service.EmailVerificationService;
import com.cuet.ghoorni.service.EmailService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private EmailService emailService;

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
            // Register user with email verification disabled initially
            user.setEmailVerified(false);
            User registeredUser = authService.registerUser(user);

            // Create verification token and send email
            EmailVerificationToken token = emailVerificationService.createVerificationToken(registeredUser.getUserId());
            emailService.sendVerificationEmail(registeredUser, token);

            return new ResponseEntity<>("Registration successful! Please check your email to verify your account.",
                    HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("An error occurred during registration. Please try again.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            String userId = loginRequest.getUserId().trim();
            System.out.println("Login attempt for user: " + userId);

            // Check if the user exists before authentication
            User user = authService.getUserByUserId(userId);
            if (user == null) {
                System.out.println("User not found: " + userId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(null, false));
            }

            System.out.println("Found user: " + user.getName() + ", UserId: " + user.getUserId());

            // Try to authenticate first
            @SuppressWarnings("unused")
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userId, loginRequest.getPassword()));

            // After successful authentication, check email verification
            if (user.getEmailVerified() == null || !user.getEmailVerified()) {
                System.out.println("Email not verified for user: " + userId);
                return ResponseEntity.ok(new LoginResponse(null, true));
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
            String jwtToken = jwtUtil.generateToken(userDetails);
            System.out.println("JWT generated with subject: " + jwtUtil.extractUsername(jwtToken));

            return ResponseEntity.ok(new LoginResponse(jwtToken, false));
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, false));
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
            cleanUser.setEmailVerified(user.getEmailVerified());
            // Don't include password
            cleanUser.setPassword(null);

            return ResponseEntity.ok(cleanUser);
        } else {
            System.out.println("/me endpoint: User not found in database for userId: " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            boolean isVerified = emailVerificationService.verifyEmail(token);

            if (isVerified) {
                return new ResponseEntity<>("Email verified successfully! You can now log in to your account.",
                        HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid or expired verification token.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            System.err.println("Email verification error: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("An error occurred during email verification.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String userId) {
        try {
            boolean isSent = emailVerificationService.resendVerificationEmail(userId);

            if (isSent) {
                return new ResponseEntity<>("Verification email sent successfully! Please check your email.",
                        HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                        "Unable to send verification email. User not found or email already verified.",
                        HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            System.err.println("Resend verification email error: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("An error occurred while sending verification email.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/verification-status")
    public ResponseEntity<?> getVerificationStatus(@RequestParam String userId) {
        try {
            User user = authService.getUserByUserId(userId);
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            boolean isVerified = user.getEmailVerified() != null && user.getEmailVerified();
            return ResponseEntity.ok().body(new VerificationStatusResponse(user.getEmail(), isVerified));
        } catch (Exception e) {
            System.err.println("Get verification status error: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("An error occurred while checking verification status.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Inner class for login response
    public static class LoginResponse {
        private String token;
        private boolean needsEmailVerification;

        public LoginResponse(String token, boolean needsEmailVerification) {
            this.token = token;
            this.needsEmailVerification = needsEmailVerification;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public boolean isNeedsEmailVerification() {
            return needsEmailVerification;
        }

        public void setNeedsEmailVerification(boolean needsEmailVerification) {
            this.needsEmailVerification = needsEmailVerification;
        }
    }

    // Inner class for verification status response
    public static class VerificationStatusResponse {
        private String email;
        private boolean verified;

        public VerificationStatusResponse(String email, boolean verified) {
            this.email = email;
            this.verified = verified;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }
    }
}