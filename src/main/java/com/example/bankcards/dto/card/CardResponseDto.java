package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Ответ с данными карты")
@Builder
public record CardResponseDto(

        @Schema(description = "ID карты", example = "1")
        Long id,

        @Schema(description = "Номер карты (скрыт кроме последних 4 цифр)", example = "**** **** **** 1234")
        String cardNumber,

        @Schema(description = "Владелец карты", example = "Ivan Ivanov")
        String placeholder,

        @Schema(description = "Срок действия карты", example = "2027-11-30")
        LocalDate expirationDate,

        @Schema(description = "Статус карты (ACTIVE, BLOCKED, EXPIRED, REQUESTED_BLOCKED)", example = "ACTIVE")
        CardStatus status,

        @Schema(description = "Баланс на карте", example = "23701.52")
        BigDecimal balance,

        @Schema(description = "ID владельца карты", example = "1")
        Long userId
) {
}
