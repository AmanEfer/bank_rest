package com.example.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на блокировку карты")
public record RequestCardBlockDto(

        @Schema(description = "Причина блокировки", example = "Данные карты похитили мошенники")
        @NotBlank(message = "Укажите причину блокировки карты")
        String reason
) {
}
