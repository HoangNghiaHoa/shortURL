package com.urlshortener.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name") // Phải có cái này để khớp với schema.sql
    private String fullName;

    @Column(name = "role", nullable = false)
    private String role; // Bỏ phần gán bằng ở đây, mình sẽ gán trong Service
    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "is_active")
    private Boolean isActive;
}