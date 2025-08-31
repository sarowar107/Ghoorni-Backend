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
        user.setEmailVerified(true);
        userRepository.save(user);

        // Delete the used token
        tokenRepository.delete(verificationToken);

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

        try {
            EmailVerificationToken token = createVerificationToken(userId);
            emailService.sendVerificationEmail(user, token);
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
    }
}
