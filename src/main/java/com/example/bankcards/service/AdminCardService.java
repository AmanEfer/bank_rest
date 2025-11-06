package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminCardService {

    private static final Random RANDOM = new Random();

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardResponseDto createNewCard(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        String cardNumber;

        do {
            cardNumber = generateCardNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));


        var newCard = Card.builder()
                .cardNumber(generateCardNumber())
                .placeholder(user.getFirstName() + " " + user.getLastName())
                .expirationDate(LocalDate.now().plusYears(10))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .user(user)
                .build();

        newCard = cardRepository.saveAndFlush(newCard);

        return toDto(newCard, user.getId());
    }

    public String blockCard(Long id) {
        var card = getCard(id);

        if (card.getStatus() == CardStatus.BLOCKED)
            throw new IllegalArgumentException("Карта уже была заблокирована ранее");

        card.setStatus(CardStatus.BLOCKED);

        return "Карта %s заблокирована".formatted(card.getCardNumber());
    }

    public String activateCard(Long id) {
        var card = getCard(id);

        if (card.getStatus() == CardStatus.ACTIVE)
            throw new IllegalArgumentException("Карта уже активирована");

        card.setStatus(CardStatus.ACTIVE);

        return "Карта %s активирована".formatted(card.getCardNumber());
    }

    public String deleteCard(Long id) {
        var card = getCard(id);

        cardRepository.delete(card);

        return "Карта %s удалена".formatted(card.getCardNumber());
    }

    @Transactional(readOnly = true)
    public PageCardResponseDto getAllCards(Pageable pageable) {

        Page<CardResponseDto> page = cardRepository.findAll(pageable)
                .map(card -> toDto(card, card.getUser().getId()));

        return toPageDto(page);
    }

    public PageCardResponseDto getUserCards(Long id, Pageable pageable) {
        Page<CardResponseDto> page = cardRepository.getCardsByUserId(id, pageable)
                .map(card -> toDto(card, id));

        return toPageDto(page);
    }

    private Card getCard(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Карта с ID %d не найдена".formatted(id)));
    }

    private String generateCardNumber() {
        var sb = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            if (i != 0 && i % 4 == 0)
                sb.append(" ");

            sb.append(RANDOM.nextInt(10));
        }

        return sb.toString();
    }

    private CardResponseDto toDto(Card card, Long userId) {
        return CardResponseDto.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .placeholder(card.getPlaceholder())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .userId(userId)
                .build();
    }

    private PageCardResponseDto toPageDto(Page<CardResponseDto> page) {
        return PageCardResponseDto.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .last(page.isLast())
                .build();
    }
}
