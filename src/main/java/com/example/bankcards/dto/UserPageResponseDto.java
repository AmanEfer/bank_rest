package com.example.bankcards.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UserPageResponseDto(

        List<UserResponseDto> content,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean last
) {
}
