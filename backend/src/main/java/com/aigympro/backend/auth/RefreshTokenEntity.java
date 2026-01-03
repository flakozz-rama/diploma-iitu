package com.aigympro.backend.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens",
        indexes = {
                @Index(name="idx_refresh_tokens_user", columnList="user_id"),
                @Index(name="idx_refresh_tokens_expires", columnList="expires_at"),
                @Index(name="uq_refresh_tokens_hash", columnList="token_hash", unique = true)
        })
public class RefreshTokenEntity {

    @Getter
    @Id @GeneratedValue
    private UUID id;

    @Setter
    @Getter
    @Column(name="user_id", nullable=false)
    private UUID userId;

    @Setter
    @Getter
    @Column(name="token_hash", nullable=false, length = 128, unique = true)
    private String tokenHash;

    @Column(name="issued_at", nullable=false)
    private OffsetDateTime issuedAt = OffsetDateTime.now();

    @Setter
    @Getter
    @Column(name="expires_at", nullable=false)
    private OffsetDateTime expiresAt;

    @Setter
    @Getter
    @Column(name="revoked", nullable=false)
    private boolean revoked = false;

    @Setter
    @Getter
    @Column(name="rotated_from_id")
    private UUID rotatedFromId;

}
