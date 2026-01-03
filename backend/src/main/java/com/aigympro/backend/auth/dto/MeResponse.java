package com.aigympro.backend.auth.dto;

import java.util.List;

public record MeResponse(
        String id,
        String email,
        List<String> roles
) {}
