package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.exception.RejectOperationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMasker;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.Encryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminCardService {

    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь не найден";

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final Encryptor encryptor;
    private final CardNumberGenerator cardNumberGenerator;


    public CardResponseDto createNewCard(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()));

        if (isAdmin)
            throw new RejectOperationException("Операция отклонена. Администратор не может быть получателем карты");

        String cardNumber;
        do {
            cardNumber = cardNumberGenerator.generate();
        } while (cardRepository.existsByEncryptedCardNumber(encryptor.encrypt(cardNumber)));

        var newCard = Card.builder()
                .cardNumber(cardNumber)
                .last4(cardNumber.substring(cardNumber.length() - 4))
                .placeholder(user.getFirstName() + " " + user.getLastName())
                .expirationDate(LocalDate.now().plusYears(10))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .user(user)
                .build();

        newCard = cardRepository.saveAndFlush(newCard);

        user.getCards().add(newCard);

        return toDto(newCard, user.getId());
    }


    public String blockCard(Long id) {
        var card = getCard(id);

        if (card.getStatus() != CardStatus.REQUESTED_BLOCKED)
            throw new CardStatusException("Для блокировки карты необходимо отправить запрос на блокировку");

        card.setStatus(CardStatus.BLOCKED);

        return "Карта %s заблокирована".formatted(mask(card.getCardNumber()));
    }


    public String activateCard(Long id) {
        var card = getCard(id);

        if (card.getStatus() == CardStatus.ACTIVE)
            throw new CardStatusException("Карта уже активирована");

        card.setStatus(CardStatus.ACTIVE);

        return "Карта %s активирована".formatted(mask(card.getCardNumber()));
    }


    public String deleteCard(Long id) {
        var card = getCard(id);

        cardRepository.delete(card);

        return "Карта %s удалена".formatted(mask(card.getCardNumber()));
    }


    @Transactional(readOnly = true)
    public PageCardResponseDto getAllCards(Pageable pageable) {

        Page<CardResponseDto> page = cardRepository.findAll(pageable)
                .map(card -> toDto(card, card.getUser().getId()));

        return toPageDto(page);
    }


    public PageCardResponseDto getUserCards(Long id, Pageable pageable) {
        if (!userRepository.existsById(id))
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);

        Page<CardResponseDto> page = cardRepository.findCardsByUserId(id, pageable)
                .map(card -> toDto(card, id));

        return toPageDto(page);
    }


    private Card getCard(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() ->
                        new CardNotFoundException("Карта с ID %d не найдена".formatted(id)));
    }


    private CardResponseDto toDto(Card card, Long userId) {
        return CardResponseDto.builder()
                .id(card.getId())
                .cardNumber(mask(card.getCardNumber()))
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


    private String mask(String cardNumber) {
        return CardMasker.maskCard(cardNumber);
    }
}
