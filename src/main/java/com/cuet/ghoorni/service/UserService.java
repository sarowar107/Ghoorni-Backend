package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Updates user's name
     * 
     * @param userId User ID
     * @param name   New name
     * @return Updated user
     */
    public User updateName(String userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(name);
        return userRepository.save(user);
    }

    /**
     * Updates user's password
     * 
     * @param userId          User ID
     * @param currentPassword Current password
     * @param newPassword     New password
     * @return True if successful
     */
    public boolean updatePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    /**
     * Deletes a user account and all associated data
     * 
     * @param userId User ID
     */
    @Transactional
    public void deleteAccount(String userId) {
        // Get the user first to ensure it exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            // The user entity has cascade=ALL on its relationships, so this will
            // automatically delete all associated entities:
            // - Notices (cascade)
            // - Files (cascade)
            // - Questions (cascade)
            // - Answers (cascade)
            userRepository.delete(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user account: " + e.getMessage());
        }
    }
}
