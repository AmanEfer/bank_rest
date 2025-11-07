package com.example.bankcards.controller;

import com.example.bankcards.dto.card.FundsDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.dto.card.RequestCardBlockDto;
import com.example.bankcards.dto.card.ResponseCardBlockDto;
import com.example.bankcards.dto.card.TransferDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.UserCardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserCardController {

    private final UserCardService userCardService;


    @GetMapping("{userId}")
    public PageCardResponseDto searchUserCards(
            Pageable pageable,
            @PathVariable
            Long userId,
            @RequestParam(required = false)
            Long cardId,
            @RequestParam(required = false)
            @Pattern(regexp = "^\\d{4}$",
                    message = "Поле должно содержать только последние 4 цифры номера карты")
            String last4,
            @RequestParam(required = false)
            CardStatus status
    ) {
        return userCardService.searchUserCards(pageable, userId, cardId, last4, status);
    }


    @GetMapping("/{userId}/balance/{cardId}")
    public ResponseEntity<String> checkBalance(
            @PathVariable Long userId,
            @PathVariable Long cardId
    ) {
        var message = userCardService.showBalance(cardId, userId);

        return ResponseEntity.ok(message);
    }


    @PatchMapping("/{userId}/balance/{cardId}/deposit")
    public ResponseEntity<String> deposit(
            @PathVariable Long userId,
            @PathVariable Long cardId,
            @Valid @RequestBody FundsDto fundsDto
    ) {
        var message = userCardService.deposit(userId, cardId, fundsDto);

        return ResponseEntity.ok(message);
    }


    @PatchMapping("/{userId}/balance/{cardId}/withdraw")
    public ResponseEntity<String> withdraw(
            @PathVariable Long userId,
            @PathVariable Long cardId,
            @Valid @RequestBody FundsDto fundsDto
    ) {
        var message = userCardService.withdraw(userId, cardId, fundsDto);

        return ResponseEntity.ok(message);
    }


    @PatchMapping("/{userId}/transfer")
    public ResponseEntity<String> transferToOwnCard(
            @PathVariable Long userId,
            @Valid @RequestBody TransferDto transferDto
    ) {
        String message = userCardService.transferToOwnCard(userId, transferDto);

        return ResponseEntity.ok(message);
    }


    @PatchMapping("/{userId}/cards/{cardId}/block")
    public ResponseEntity<ResponseCardBlockDto> requestCardBlock(
            @PathVariable Long userId,
            @PathVariable Long cardId,
            @Valid @RequestBody RequestCardBlockDto blockDto
    ) {
        var message = userCardService.blockCard(userId, cardId, blockDto);

        return ResponseEntity.ok(message);
    }

}
