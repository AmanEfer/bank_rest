package com.example.bankcards.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на обновление данных пользователя")
public record UserUpdateRequestDto(

        @Schema(description = "Имя пользователя", example = "Иван")
        @NotBlank(message = "Поле с именем не может быть пустым")
        String firstName,

        @Schema(description = "Фамилия пользователя", example = "Иванов")
        @NotBlank(message = "Поле с фамилией не может быть пустым")
        String lastName
) {
}
