package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.EmailVerificationToken;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.repository.EmailVerificationTokenRepository;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Map to track last email send time per user (in production, use Redis or
    // database)
    private final java.util.concurrent.ConcurrentHashMap<String, Long> lastEmailSentMap = new java.util.concurrent.ConcurrentHashMap<>();

    public EmailVerificationToken createVerificationToken(String userId) {
        // Delete any existing tokens for this user
        tokenRepository.deleteByUserId(userId);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24); // Token expires in 24 hours

        EmailVerificationToken verificationToken = new EmailVerificationToken(token, userId, expiryDate);
        return tokenRepository.save(verificationToken);
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<EmailVerificationToken> tokenOptional = tokenRepository.findByToken(token);

        if (tokenOptional.isEmpty()) {
            return false; // Token not found
        }

        EmailVerificationToken verificationToken = tokenOptional.get();

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            return false; // Token expired
        }

        // Find user and update email verification status
        Optional<User> userOptional = userRepository.findById(verificationToken.getUserId());
        if (userOptional.isEmpty()) {
            return false; // User not found
        }

        User user = userOptional.get();
        System.out.println(
                "Before verification - User: " + user.getUserId() + ", EmailVerified: " + user.getEmailVerified());

        user.setEmailVerified(true);
        User savedUser = userRepository.save(user);
        System.out.println("After verification - User: " + savedUser.getUserId() + ", EmailVerified: "
                + savedUser.getEmailVerified());

        // Delete the used token
        tokenRepository.delete(verificationToken);
        System.out.println("Email verification token deleted for user: " + user.getUserId());

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user);
        } catch (Exception e) {
            // Log but don't fail verification if welcome email fails
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return true;
    }

    public boolean resendVerificationEmail(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        if (user.getEmailVerified()) {
            return false; // Email already verified
        }

        // Rate limiting: prevent sending emails too frequently (within 30 seconds)
        Long lastSentTime = lastEmailSentMap.get(userId);
        long currentTime = System.currentTimeMillis();

        if (lastSentTime != null && (currentTime - lastSentTime) < 30000) { // 30 seconds
            System.out.println("Email send request blocked for user " + userId + " - too frequent");
            return false; // Too frequent, block the request
        }

        try {
            EmailVerificationToken token = createVerificationToken(userId);
            emailService.sendVerificationEmail(user, token);

            // Update the last sent time
            lastEmailSentMap.put(userId, currentTime);

            return true;
        } catch (Exception e) {
            System.err.println("Failed to resend verification email: " + e.getMessage());
            return false;
        }
    }

    public boolean isEmailVerified(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.map(user -> user.getEmailVerified() != null && user.getEmailVerified()).orElse(false);
    }

    // Scheduled task to clean up expired tokens (runs every hour)
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        System.out.println("Cleaned up expired email verification tokens at: " + LocalDateTime.now());

        // Also cleanup old entries from rate limiting map (older than 1 hour)
        long oneHourAgo = System.currentTimeMillis() - 3600000;
        lastEmailSentMap.entrySet().removeIf(entry -> entry.getValue() < oneHourAgo);
        System.out.println("Cleaned up old email rate limiting entries");
    }
}
