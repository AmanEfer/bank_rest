package com.example.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Запрос на перевод денежных средств между своими картами")
public record TransferDto(

        @Schema(description = "ID карты отправителя (не может быть пустым)", example = "1")
        @NotNull(message = "Поле from не может быть пустым")
        Long from,

        @Schema(description = "ID карты получателя (не может быть пустым)", example = "2")
        @NotNull(message = "Поле to не может быть пустым")
        Long to,

        @Schema(description = "Сумма перевода (должна быть положительной)", example = "999.99")
        @NotNull(message = "Поле с суммой не может быть пустой")
        @Positive(message = "Сумма не может быть отрицательной или 0")
        BigDecimal funds
) {
}
