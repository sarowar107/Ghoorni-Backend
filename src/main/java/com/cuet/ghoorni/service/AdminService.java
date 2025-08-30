package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Checks if a user has admin role
     * 
     * @param userId User ID to check
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return "admin".equalsIgnoreCase(user.getRole());
    }

    /**
     * Gets all users in the system
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Deletes a user by ID
     * 
     * @param userId ID of the user to delete
     */
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}
