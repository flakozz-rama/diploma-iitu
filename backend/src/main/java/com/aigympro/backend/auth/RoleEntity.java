package com.aigympro.backend.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class RoleEntity {
    @Getter
    @Id @GeneratedValue
    private UUID id;

    @Setter
    @Getter
    @Column(nullable = false, unique = true)
    private String code; // USER/COACH/ADMIN

    @Column(nullable = false)
    private String name;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

}
