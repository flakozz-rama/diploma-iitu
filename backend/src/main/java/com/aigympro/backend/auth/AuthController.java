package com.aigympro.backend.auth;

import com.aigympro.backend.auth.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final JwtService jwt;

    public AuthController(AuthService service, JwtService jwt) {
        this.service = service;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req){
        return service.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req){
        return service.login(req);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req){
        return service.refresh(req);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req){
        service.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public MeResponse me(Authentication auth){
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Object principal = auth.getPrincipal();
        UUID userId;

        if (principal instanceof UUID u) {
            userId = u;
        } else if (principal instanceof String s) {
            userId = UUID.fromString(s);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return service.me(userId);
    }
}
