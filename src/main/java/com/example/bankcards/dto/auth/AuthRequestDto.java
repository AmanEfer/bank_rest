package com.example.bankcards.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на вход пользователя в систему")
public record AuthRequestDto(

        @Schema(
                description = "Номер телефона для регистрации. Должно состоять из 10 цифр без префикса",
                example = "9261234567"
        )
        String phoneNumber,

        @Schema(
                description = "Пароль должен быть не менее 6 символов и содержать цифры, латинские буквы и символы !@#$%^&",
                example = "parol12345@#!"
        )
        String password
) {
}
