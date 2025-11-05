package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record UserRegisterRequestDto(

        @NotBlank(message = "Поле с именем не может быть пустым")
        String firstName,

        @NotBlank(message = "Поле с фамилией не может быть пустым")
        String lastName,

        @NotBlank(message = "Поле с номером телефона не может быть пустым")
        @Pattern(regexp = "^\\d{10}$", message = "Поле с номером телефона должно состоять из 10 цифр без префикса")
        String phoneNumber,

        @NotBlank(message = "Поле с паролем не может быть пустым")
        @Size(min = 6, message = "Пароль должен быть содержать как минимум 6 символов")
        @Pattern(
                regexp = "^[A-Za-z0-9!@#$%^&]+$",
                message = "Пароль может содержать цифры, латинские буквы и символы !@#$%^&"
        )
        String password
) {
}
