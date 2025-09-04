package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.Notification;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.repository.FileRepository;
import com.cuet.ghoorni.repository.UserRepository;
import com.google.api.services.drive.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GoogleDriveService googleDriveService;

    public com.cuet.ghoorni.model.Files storeFile(MultipartFile file, String topic, String category, boolean isPublic,
            String userId,
            String toDept, String toBatch)
            throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Upload to Google Drive
        String driveFileId = googleDriveService.uploadFile(file, fileName);

        // Get Google Drive file metadata
        File driveFile = googleDriveService.getFileMetadata(driveFileId);

        User uploadedBy = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));

        com.cuet.ghoorni.model.Files fileEntity = new com.cuet.ghoorni.model.Files();
        fileEntity.setTopic(topic);
        fileEntity.setCategory(category);
        fileEntity.setContent(fileName); // Keep for backward compatibility
        fileEntity.setOriginalFilename(file.getOriginalFilename());
        fileEntity.setFileSize(file.getSize());
        fileEntity.setMimeType(file.getContentType());
        fileEntity.setDriveFileId(driveFileId);
        fileEntity.setDriveViewLink(driveFile.getWebViewLink());
        fileEntity.setDriveDownloadLink(driveFile.getWebContentLink());
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

    public String getFileDownloadLink(Long fileId) throws IOException {
        com.cuet.ghoorni.model.Files fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id " + fileId));

        // Return Google Drive download link if available, otherwise try to generate one
        if (fileEntity.getDriveDownloadLink() != null && !fileEntity.getDriveDownloadLink().isEmpty()) {
            return fileEntity.getDriveDownloadLink();
        } else if (fileEntity.getDriveFileId() != null) {
            try {
                // Check if Google Drive service is authenticated
                if (googleDriveService.isAuthenticated()) {
                    return googleDriveService.getFileDownloadLink(fileEntity.getDriveFileId());
                } else {
                    // Fallback: construct direct Google Drive download link
                    return "https://drive.google.com/uc?export=download&id=" + fileEntity.getDriveFileId();
                }
            } catch (Exception e) {
                // Fallback: construct direct Google Drive download link
                return "https://drive.google.com/uc?export=download&id=" + fileEntity.getDriveFileId();
            }
        } else {
            throw new RuntimeException("File not available for download");
        }
    }

    public String getFileViewLink(Long fileId) throws IOException {
        com.cuet.ghoorni.model.Files fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id " + fileId));

        // Return Google Drive view link if available, otherwise try to generate one
        if (fileEntity.getDriveViewLink() != null && !fileEntity.getDriveViewLink().isEmpty()) {
            return fileEntity.getDriveViewLink();
        } else if (fileEntity.getDriveFileId() != null) {
            try {
                // Check if Google Drive service is authenticated
                if (googleDriveService.isAuthenticated()) {
                    return googleDriveService.getFileViewLink(fileEntity.getDriveFileId());
                } else {
                    // Fallback: construct direct Google Drive view link
                    return "https://drive.google.com/file/d/" + fileEntity.getDriveFileId() + "/view";
                }
            } catch (Exception e) {
                // Fallback: construct direct Google Drive view link
                return "https://drive.google.com/file/d/" + fileEntity.getDriveFileId() + "/view";
            }
        } else {
            throw new RuntimeException("File not available for viewing");
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

        // Delete associated notifications before deleting the file
        notificationService.deleteNotificationsByReferenceId(fileId.toString(),
                Notification.NotificationType.FILE_UPLOADED);

        // Delete the file from Google Drive
        if (file.getDriveFileId() != null && !file.getDriveFileId().isEmpty()) {
            googleDriveService.deleteFile(file.getDriveFileId());
        }

        // Delete the database record
        fileRepository.delete(file);
    }
}