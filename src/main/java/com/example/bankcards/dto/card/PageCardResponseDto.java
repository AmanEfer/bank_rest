package com.example.bankcards.dto.card;

import lombok.Builder;

import java.util.List;

@Builder
public record PageCardResponseDto(

        List<CardResponseDto> content,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean last
) {
}
