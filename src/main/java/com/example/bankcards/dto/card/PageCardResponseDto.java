package com.example.bankcards.dto.card;

import lombok.Builder;

import java.util.List;

@Builder
public record PageCardResponseDto(

        List<CardResponseDto> content,
        Integer page,
        Integer size,
        Integer totalPages,
        Long totalElements,
        Boolean last
) {
}
