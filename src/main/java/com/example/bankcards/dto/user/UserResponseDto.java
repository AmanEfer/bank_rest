package com.example.bankcards.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Ответ с данными пользователя")
@Builder
public record UserResponseDto(

        @Schema(description = "ID пользователя", example = "1")
        Long id,

        @Schema(description = "Имя пользователя", example = "Иван")
        String firstName,

        @Schema(description = "Фамилия пользователя", example = "Иванов")
        String lastName,

        @Schema(description = "Номер телефона пользователя", example = "9261234567")
        String phoneNumber
) {
}
