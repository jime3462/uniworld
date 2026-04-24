package com.uniworld.uniworld_backend.dto;

public record AuthResponse(
        String token,
        Long userID,
        String name,
        String email,
        String role
) {
}
