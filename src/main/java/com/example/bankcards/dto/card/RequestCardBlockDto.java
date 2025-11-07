package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;

public record RequestCardBlockDto(
        @NotBlank(message = "Укажите причину блокировки карты")
        String reason
) {
}
