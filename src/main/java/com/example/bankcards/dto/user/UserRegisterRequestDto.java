package com.example.bankcards.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрс на регистрацию нового пользователя")
public record UserRegisterRequestDto(

        @Schema(description = "Имя пользователя", example = "Иван")
        @NotBlank(message = "Поле с именем не может быть пустым")
        String firstName,

        @Schema(description = "Фамилия пользователя", example = "Иванов")
        @NotBlank(message = "Поле с фамилией не может быть пустым")
        String lastName,

        @Schema(
                description = "Номер телефона для регистрации. Должно состоять из 10 цифр без префикса",
                example = "9261234567"
        )
        @NotBlank(message = "Поле с номером телефона не может быть пустым")
        @Pattern(regexp = "^\\d{10}$", message = "Поле с номером телефона должно состоять из 10 цифр без префикса")
        String phoneNumber,


        @Schema(
                description = "Пароль должен быть не менее 6 символов и содержать цифры, латинские буквы и символы !@#$%^&",
                example = "parol12345@#!"
        )
        @NotBlank(message = "Поле с паролем не может быть пустым")
        @Size(min = 6, message = "Пароль должен быть содержать как минимум 6 символов")
        @Pattern(
                regexp = "^[A-Za-z0-9!@#$%^&]+$",
                message = "Пароль может содержать цифры, латинские буквы и символы !@#$%^&"
        )
        String password
) {
}
