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
    private String content; // Assuming file path or some identifier

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