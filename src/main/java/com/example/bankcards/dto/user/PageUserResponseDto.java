package com.example.bankcards.dto.user;

import lombok.Builder;

import java.util.List;

@Builder
public record PageUserResponseDto(

        List<UserResponseDto> content,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean last
) {
}
