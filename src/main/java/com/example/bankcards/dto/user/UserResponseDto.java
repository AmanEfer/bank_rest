package com.example.bankcards.dto.user;

import lombok.Builder;

@Builder
public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber
) {
}
