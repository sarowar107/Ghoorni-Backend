package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) throws IllegalArgumentException {
        // Check if user already exists
        if (userRepository.existsById(user.getUserId())) {
            throw new IllegalArgumentException("User ID already exists. Please verify your ID.");
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUserByUserId(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            // Try to find by ID case-insensitive as a fallback (might be causing your
            // issue)
            System.out.println("User not found by exact ID. Attempting to find all users with similar ID...");

            // Print all users in the database for debugging
            userRepository.findAll().forEach(u -> {
                System.out.println("User in DB: " + u.getUserId() + ", Name: " + u.getName());
            });
        }
        return user;
    }
}