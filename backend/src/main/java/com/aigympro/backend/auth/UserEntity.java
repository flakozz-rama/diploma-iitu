package com.aigympro.backend.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    @Getter
    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @Getter
    @Column(name = "email", nullable = false, unique = true/* citext в БД */)
    private String email;

    @Setter
    @Getter
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Setter
    @Getter
    @Column(name = "is_email_verified", nullable = false)
    private boolean emailVerified = false;

    @Setter
    @Getter
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }
    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

}
