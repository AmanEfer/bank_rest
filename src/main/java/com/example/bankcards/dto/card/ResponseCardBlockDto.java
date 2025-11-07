package com.example.bankcards.dto.card;

import lombok.Builder;

@Builder
public record ResponseCardBlockDto(
        CardResponseDto card,
        String reason
) {
}
