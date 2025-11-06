package com.example.bankcards.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequestDto(
        @NotBlank(message = "Поле с именем не может быть пустым")
        String firstName,

        @NotBlank(message = "Поле с фамилией не может быть пустым")
        String lastName
) {
}
