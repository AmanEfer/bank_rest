package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.service.AdminCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Админ / карты", description = "Управление картами (только ADMIN)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Ошибка аутентификации (невалидный токен"),
        @ApiResponse(responseCode = "403", description = "Ошибка авторизации (только ADMIN)")
})
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("api/v1/admin/cards")
@RequiredArgsConstructor
public class AdminCardController {

    private final AdminCardService adminCardService;


    @Operation(summary = "Выпустить новую карту ")
    @ApiResponse(responseCode = "200", description = "Карта успешно выпущена")
    @PostMapping
    public CardResponseDto issueNewCard(@RequestParam Long userId) {
        return adminCardService.createNewCard(userId);
    }


    @Operation(summary = "Заблокировать карту, если был запрос от пользователя (статус \"REQUESTED_BLOCKED\")")
    @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована")
    @PatchMapping("/{id}/block")
    public ResponseEntity<String> blockCard(@PathVariable Long id) {
        var message = adminCardService.blockCard(id);

        return ResponseEntity.ok(message);
    }


    @Operation(summary = "Активировать карту")
    @ApiResponse(responseCode = "200", description = "Карта успешно активирована")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateCard(@PathVariable Long id) {
        String message = adminCardService.activateCard(id);

        return ResponseEntity.ok(message);
    }


    @Operation(summary = "Удалить карту")
    @ApiResponse(responseCode = "200", description = "Карта успешно удалена")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCard(@PathVariable Long id) {
        String message = adminCardService.deleteCard(id);

        return ResponseEntity.ok(message);
    }


    @Operation(summary = "Получить список всех карт (с пагинацией)")
    @ApiResponse(responseCode = "200", description = "Список карт")
    @GetMapping
    public PageCardResponseDto viewAllCards(Pageable pageable) {
        return adminCardService.getAllCards(pageable);
    }


    @Operation(summary = "Получить список карт (с пагинацией) конкретного пользователя (по ID)")
    @ApiResponse(responseCode = "200", description = "Список карт пользователя")
    @GetMapping("/user/{id}")
    public PageCardResponseDto viewUserCards(@PathVariable Long id, Pageable pageable) {
        return adminCardService.getUserCards(id, pageable);
    }
}
