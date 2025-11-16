package com.example.bankcards.controller;

import com.example.bankcards.dto.card.FundsDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.dto.card.RequestCardBlockDto;
import com.example.bankcards.dto.card.ResponseCardBlockDto;
import com.example.bankcards.dto.card.TransferDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.UserCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Пользователь / карты", description = "Управление картами (только USER)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Ошибка аутентификации (невалидный токен"),
        @ApiResponse(responseCode = "403", description = "Ошибка авторизации (только USER)")
})
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("api/v1/users/cards")
@RequiredArgsConstructor
@Validated
public class UserCardController {

    private final UserCardService userCardService;


    @Operation(summary = "Поиск карт пользователя с фильтрацией (по ID, последним 4 цифрам, статусу) и пагинацией")
    @ApiResponse(responseCode = "200", description = "Список карт")
    @GetMapping
    public PageCardResponseDto searchUserCards(
            Pageable pageable,

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @RequestParam(required = false)
            Long cardId,

            @RequestParam(required = false)
            @Pattern(regexp = "^\\d{4}$", message = "Поле должно содержать только последние 4 цифры номера карты")
            String last4,

            @RequestParam(required = false)
            CardStatus status
    ) {
        return userCardService.searchUserCards(pageable, userDetails.userId(), cardId, last4, status);
    }


    @Operation(summary = "Проверить баланс карты")
    @ApiResponse(responseCode = "200", description = "Баланс карты")
    @GetMapping("/balance/{cardId}")
    public ResponseEntity<String> checkBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cardId
    ) {
        var message = userCardService.showBalance(cardId, userDetails.userId());

        return ResponseEntity.ok(message);
    }


    @Operation(summary = "Пополнить баланс карты")
    @ApiResponse(responseCode = "200", description = "Средства зачислены")
    @PatchMapping("/balance/{cardId}/deposit")
    public ResponseEntity<String> deposit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cardId,
            @Valid @RequestBody FundsDto fundsDto
    ) {
        var message = userCardService.deposit(userDetails.userId(), cardId, fundsDto);

        return ResponseEntity.ok(message);
    }


    @Operation(summary = "Снять деньги с карты")
    @ApiResponse(responseCode = "200", description = "Деньги сняты")
    @PatchMapping("/balance/{cardId}/withdraw")
    public ResponseEntity<String> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cardId,
            @Valid @RequestBody FundsDto fundsDto
    ) {
        var message = userCardService.withdraw(userDetails.userId(), cardId, fundsDto);

        return ResponseEntity.ok(message);
    }


    @Operation(summary = "Перевод между своими картами")
    @ApiResponse(responseCode = "200", description = "Перевод осуществлен")
    @PatchMapping("/transfer")
    public ResponseEntity<String> transferToOwnCard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TransferDto transferDto
    ) {
        String message = userCardService.transferToOwnCard(userDetails.userId(), transferDto);

        return ResponseEntity.ok(message);
    }


    @Operation(summary = "Запросить блокировку карты")
    @ApiResponse(responseCode = "200", description = "Блокировка запрошена")
    @PatchMapping("/cards/{cardId}/block")
    public ResponseEntity<ResponseCardBlockDto> requestCardBlock(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cardId,
            @Valid @RequestBody RequestCardBlockDto blockDto
    ) {
        var message = userCardService.blockCard(userDetails.userId(), cardId, blockDto);

        return ResponseEntity.ok(message);
    }

}
