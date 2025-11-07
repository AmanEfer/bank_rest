package com.example.bankcards.dto.user;

import lombok.Builder;

import java.util.List;

@Builder
public record PageUserResponseDto(

        List<UserResponseDto> content,
        Integer page,
        Integer size,
        Integer totalPages,
        Long totalElements,
        Boolean last
) {
}
