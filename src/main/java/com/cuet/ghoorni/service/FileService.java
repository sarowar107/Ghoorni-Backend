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

    public com.cuet.ghoorni.model.Files storeFile(MultipartFile file, String topic, boolean isPublic, String userId)
            throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        java.nio.file.Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        User uploadedBy = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));

        com.cuet.ghoorni.model.Files fileEntity = new com.cuet.ghoorni.model.Files();
        fileEntity.setTopic(topic);
        fileEntity.setContent(fileName); // Store just the file name/path
        fileEntity.setUploadedBy(uploadedBy);
        fileEntity.setPublic(isPublic);
        fileEntity.setUploadedAt(LocalDateTime.now());

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

    // New method to get all file metadata
    public List<com.cuet.ghoorni.model.Files> getAllFiles() {
        return fileRepository.findAll();
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