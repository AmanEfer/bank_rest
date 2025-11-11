package com.example.bankcards.dto.auth;

public record AuthRequestDto(
        String phoneNumber,
        String password
) {
}
