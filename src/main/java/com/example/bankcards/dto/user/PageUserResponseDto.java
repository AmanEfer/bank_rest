package com.example.bankcards.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "Постраничный вывод списка пользователей")
@Builder
public record PageUserResponseDto(

        @Schema(description = "Список пользователей на текущей странице")
        List<UserResponseDto> content,

        @Schema(description = "Номер страницы", example = "5")
        Integer page,

        @Schema(description = "Количество выводимых пользователей на странице", example = "3")
        Integer size,

        @Schema(description = "Общее количество страниц", example = "25")
        Integer totalPages,

        @Schema(description = "Общее количество зарегистрированных пользователей", example = "56354")
        Long totalElements,

        @Schema(description = "Является ли выводимая страница последней (true/false)", example = "false")
        Boolean last
) {
}
