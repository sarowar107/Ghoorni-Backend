package com.cuet.ghoorni.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "dept_name")
    private String deptName;

    @Column(name = "batch")
    private String batch;

    @Column(name = "role")
    private String role; // e.g., "student", "cr", "teacher", "admin"

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Notice> notices;

    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Files> files;

    @OneToMany(mappedBy = "askedBy", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Question> questions;

    @OneToMany(mappedBy = "answeredBy", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Answer> answers;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EmailVerificationToken> emailVerificationTokens;

    // Getters and setters from Lombok @Data
}