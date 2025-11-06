package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CardResponseDto(
        Long id,
        String cardNumber,
        String placeholder,
        LocalDate expirationDate,
        CardStatus status,
        BigDecimal balance,
        Long userId
) {
}
