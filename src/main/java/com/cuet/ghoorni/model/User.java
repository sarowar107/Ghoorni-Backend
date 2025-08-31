package com.cuet.ghoorni.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Table(name = "user")
public class User {
    @Id
    private String userId;

    private String name;
    private String email;
    private String password;
    private String deptName;
    private String batch;
    private String role; // e.g., "student", "cr", "teacher", "admin"

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean emailVerified = true;

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

    // Getters and setters from Lombok @Data
}