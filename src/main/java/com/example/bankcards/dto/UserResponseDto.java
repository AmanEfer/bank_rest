package com.example.bankcards.dto;

import lombok.Builder;

@Builder
public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber
) {
}
