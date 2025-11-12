package com.example.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Запрос на операцию по пополнению / снятию / переводу денег")
public record FundsDto(

        @Schema(description = "Сумма операции (должна быть положительной)", example = "5000.00")
        @NotNull(message = "Поле с суммой не может быть пустой")
        @Positive(message = "Сумма не может быть отрицательной или 0")
        BigDecimal funds
) {
}
