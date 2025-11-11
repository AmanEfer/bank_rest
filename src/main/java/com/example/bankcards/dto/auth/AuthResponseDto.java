package com.example.bankcards.dto.auth;

import lombok.Builder;

import java.util.Set;

@Builder
public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String firstName,
        String lastName,
        Set<String> roles
) {
}
