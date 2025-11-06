package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.service.AdminCardService;
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

@RestController
@RequestMapping("api/v1/admin/cards")
@RequiredArgsConstructor
public class AdminCardController {

    private final AdminCardService adminCardService;

    @PostMapping
    public CardResponseDto issueNewCard(@RequestParam Long userId) {
        return adminCardService.createNewCard(userId);
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<String> blockCard(@PathVariable Long id) {
        var message = adminCardService.blockCard(id);

        return ResponseEntity.ok(message);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateCard(@PathVariable Long id) {
        String message = adminCardService.activateCard(id);

        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCard(@PathVariable Long id) {
        String message = adminCardService.deleteCard(id);

        return ResponseEntity.ok(message);
    }

    @GetMapping
    public PageCardResponseDto viewAllCards(Pageable pageable) {
        return adminCardService.getAllCards(pageable);
    }

    @GetMapping("/user/{id}")
    public PageCardResponseDto viewUserCards(@PathVariable Long id, Pageable pageable) {
        return adminCardService.getUserCards(id, pageable);
    }
}
