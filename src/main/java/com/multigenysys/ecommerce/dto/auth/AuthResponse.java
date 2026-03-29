package com.multigenysys.ecommerce.dto.auth;

public record AuthResponse(
        String token,
        String email,
        String role
) {
}
