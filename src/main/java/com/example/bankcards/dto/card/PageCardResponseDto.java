package com.example.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "Ответ с данными карт пользователя(-ей)")
@Builder
public record PageCardResponseDto(

        @Schema(description = "Данные карт пользователя(-ей)")
        List<CardResponseDto> content,

        @Schema(description = "Номер страницы", example = "5")
        Integer page,

        @Schema(description = "Количество выводимых карт на странице", example = "3")
        Integer size,

        @Schema(description = "Общее количество страниц", example = "25")
        Integer totalPages,

        @Schema(description = "Общее количество карт", example = "56354")
        Long totalElements,

        @Schema(description = "Является ли выводимая страница последней (true/false)", example = "false")
        Boolean last
) {
}
