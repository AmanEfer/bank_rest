package com.example.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Ответ на блокировку карты")
@Builder
public record ResponseCardBlockDto(

        @Schema(description = "Данные карты")
        CardResponseDto card,

        @Schema(description = "Причина блокировки карты", example = "Данные карты похитили мошенники")
        String reason
) {
}
