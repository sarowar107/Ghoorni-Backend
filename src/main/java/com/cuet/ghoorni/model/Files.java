package com.cuet.ghoorni.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Table(name = "files")
public class Files {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "topic")
    private String topic;

    @Column(name = "content")
    private String content; // Google Drive file ID or local file path

    @Column(name = "original_filename")
    private String originalFilename; // Store the original filename

    @Column(name = "file_size")
    private Long fileSize; // File size in bytes

    @Column(name = "mime_type")
    private String mimeType; // File MIME type

    @Column(name = "drive_file_id")
    private String driveFileId; // Google Drive file ID

    @Column(name = "drive_view_link")
    private String driveViewLink; // Google Drive view link

    @Column(name = "drive_download_link")
    private String driveDownloadLink; // Google Drive download link

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "to_dept")
    private String toDept;

    @Column(name = "to_batch")
    private String toBatch;

    @Column(name = "category")
    private String category;

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    @JsonIgnore
    private User uploadedBy;
}