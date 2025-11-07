package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record FundsDto(
        @NotNull(message = "Поле с суммой не может быть пустой")
        @Positive(message = "Сумма не может быть отрицательной или 0")
        BigDecimal funds
) {
}
