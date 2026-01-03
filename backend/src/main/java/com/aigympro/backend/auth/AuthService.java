package com.aigympro.backend.auth;

import com.aigympro.backend.auth.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    // ВНИМАНИЕ: трактуем app.jwt.refreshTtl как "минуты"
    private final long refreshTtlMinutes;

    public AuthService(UserRepository users,
                       RoleRepository roles,
                       UserRoleRepository userRoles,
                       RefreshTokenRepository refreshTokens,
                       PasswordEncoder encoder,
                       JwtService jwt,
                       @Value("${app.jwt.refreshTtl}") long refreshTtlMinutes) {
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.refreshTokens = refreshTokens;
        this.encoder = encoder;
        this.jwt = jwt;
        this.refreshTtlMinutes = refreshTtlMinutes;
    }

    public AuthResponse register(RegisterRequest req) {
        users.findByEmailIgnoreCase(req.email()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used");
        });

        var u = new UserEntity();
        u.setEmail(req.email());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setStatus("ACTIVE");
        u = users.save(u);

        // роль USER
        var roleUser = roles.findByCode("USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role USER not found"));
        var link = new UserRoleEntity();
        link.setUserId(u.getId());
        link.setRoleId(roleUser.getId());
        userRoles.save(link);

        return issueTokens(u);
    }

    public AuthResponse login(LoginRequest req) {
        var u = users.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        return issueTokens(u);
    }

    public AuthResponse refresh(RefreshRequest req) {
        var hash = sha256(req.refreshToken());

        var rt = refreshTokens.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh"));

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh");
        }

        // ротация: помечаем старый как revoked и выдаем новый
        rt.setRevoked(true);
        refreshTokens.save(rt);

        var u = users.findById(rt.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh"));

        return issueTokens(u, Optional.of(rt.getId()));
    }

    public void logout(String refreshToken) {
        refreshTokens.findByTokenHash(sha256(refreshToken)).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokens.save(rt);
        });
    }

    public MeResponse me(UUID userId) {
        var u = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var r = userRoles.findByUserId(u.getId());
        var roleCodes = r.stream()
                .map(ur -> roles.findById(ur.getRoleId()).map(RoleEntity::getCode).orElse("?"))
                .toList();

        return new MeResponse(u.getId().toString(), u.getEmail(), roleCodes);
    }

    // ===== helpers

    private AuthResponse issueTokens(UserEntity u) {
        return issueTokens(u, Optional.empty());
    }

    private AuthResponse issueTokens(UserEntity u, Optional<UUID> rotatedFrom) {
        var roleCodes = userRoles.findByUserId(u.getId()).stream()
                .map(ur -> roles.findById(ur.getRoleId()).map(RoleEntity::getCode).orElse("?"))
                .toList();

        String access = jwt.generateAccess(u.getId(), u.getEmail(), roleCodes);

        // refresh — случайная строка, в БД кладем только sha256
        String refreshPlain = UUID.randomUUID() + "." + UUID.randomUUID();

        var rt = new RefreshTokenEntity();
        rt.setUserId(u.getId());
        rt.setTokenHash(sha256(refreshPlain));

        // FIX: refreshTtlMinutes (43200 минут = 30 дней)
        rt.setExpiresAt(OffsetDateTime.now().plusMinutes(refreshTtlMinutes));

        rotatedFrom.ifPresent(rt::setRotatedFromId);
        refreshTokens.save(rt);

        return new AuthResponse(access, refreshPlain);
    }

    private static String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
