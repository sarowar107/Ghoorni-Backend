package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.repository.FileRepository;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List; // Import the List class

@Service
public class FileService {

    private final Path fileStorageLocation = Paths.get("src/main/resources/uploads").toAbsolutePath().normalize();

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    public FileService() {
        try {
            java.nio.file.Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public com.cuet.ghoorni.model.Files storeFile(MultipartFile file, String topic, String category, boolean isPublic,
            String userId,
            String toDept, String toBatch)
            throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        java.nio.file.Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        User uploadedBy = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));

        com.cuet.ghoorni.model.Files fileEntity = new com.cuet.ghoorni.model.Files();
        fileEntity.setTopic(topic);
        fileEntity.setCategory(category);
        fileEntity.setContent(fileName); // Store just the file name/path
        fileEntity.setUploadedBy(uploadedBy);
        fileEntity.setPublic(isPublic);
        fileEntity.setUploadedAt(LocalDateTime.now());

        // Set department and batch based on user role
        if ("teacher".equalsIgnoreCase(uploadedBy.getRole())) {
            fileEntity.setToDept(uploadedBy.getDeptName());
            fileEntity.setToBatch(toBatch); // Set from parameter for teachers
        } else if ("cr".equalsIgnoreCase(uploadedBy.getRole()) || "student".equalsIgnoreCase(uploadedBy.getRole())) {
            fileEntity.setToDept(uploadedBy.getDeptName());
            fileEntity.setToBatch(uploadedBy.getBatch());
        } else if ("admin".equalsIgnoreCase(uploadedBy.getRole())) {
            // Admin can set both department and batch
            fileEntity.setToDept(toDept != null ? toDept : "ALL");
            fileEntity.setToBatch(toBatch != null ? toBatch : "1");
        }

        return fileRepository.save(fileEntity);
    }

    public Resource loadFileAsResource(Long fileId) throws IOException {
        com.cuet.ghoorni.model.Files fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id " + fileId));

        Path filePath = this.fileStorageLocation.resolve(fileEntity.getContent()).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return resource;
        } else {
            throw new RuntimeException("File not found " + fileEntity.getContent());
        }
    }

    // Get all files with filtering based on user's role and department/batch
    public List<com.cuet.ghoorni.model.Files> getAllFiles(String userId) {
        User currentUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<com.cuet.ghoorni.model.Files> allFiles = fileRepository.findAll();

        return allFiles.stream()
                .filter(file -> {
                    // File creator can always see their own files
                    if (file.getUploadedBy().getUserId().equals(userId)) {
                        return true;
                    }

                    // Admin can see all files
                    if ("admin".equalsIgnoreCase(currentUser.getRole())) {
                        return true;
                    }

                    // Public files are visible to everyone
                    if (file.isPublic()) {
                        return true;
                    }

                    // If file is targeted to specific department/batch
                    boolean isDeptMatch = file.getToDept().equals("ALL") ||
                            file.getToDept().equals(currentUser.getDeptName());
                    boolean isBatchMatch = file.getToBatch().equals("1") ||
                            file.getToBatch().equals(currentUser.getBatch());

                    return isDeptMatch && isBatchMatch;
                })
                .toList();
    }

    public void deleteFile(Long fileId, String userId) throws IOException {
        com.cuet.ghoorni.model.Files file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        // Check if the user is the uploader or an admin
        if (!file.getUploadedBy().getUserId().equals(userId) && !user.getRole().equals("admin")) {
            throw new RuntimeException("You don't have permission to delete this file");
        }

        // Delete the physical file
        Path filePath = this.fileStorageLocation.resolve(file.getContent()).normalize();
        java.nio.file.Files.deleteIfExists(filePath);

        // Delete the database record
        fileRepository.delete(file);
    }
}