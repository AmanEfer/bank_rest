package com.example.bankcards.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Schema(description = "Ответ аутентификации пользователя (с JWT-токенами)")
@Builder
public record AuthResponseDto(

        @Schema(
                description = "Access токен. Действителен 10 минут",
        example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4MDA1NTUzNTM...")
        String accessToken,

        @Schema(
                description = "Refresh токен. Действителен 24 часа",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4MDA1NTUzNTM...")
        String refreshToken,

        @Schema(description = "Имя пользователя", example = "Иван")
        String firstName,

        @Schema(description = "Фамилия пользователя", example = "Иванов")
        String lastName,

        @Schema(description = "Роли пользователя(USER/ADMIN)", example = "ROLE_USER")
        Set<String> roles
) {
}
