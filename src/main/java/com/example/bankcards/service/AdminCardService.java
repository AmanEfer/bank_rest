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
import com.example.bankcards.util.Encryptor;
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

    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь не найден";
    private static final String REQUESTED_BLOCKED_MESSAGE = "Для блокировки карты необходимо отправить запрос на блокировку";
    private static final String CARD_BLOCKED_MESSAGE = "Карта %s заблокирована";
    private static final String CARD_ALREADY_ACTIVATED_MESSAGE = "Карта уже активирована";
    private static final String CARD_SUCCESSFULLY_ACTIVATED_MESSAGE = "Карта %s активирована";
    private static final String CARD_DELETED_MESSAGE = "Карта %s удалена";
    private static final String CARD_NOT_FOUND_MESSAGE = "Карта с ID %d не найдена";

    private static final Random RANDOM = new Random();

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final Encryptor encryptor;


    public CardResponseDto createNewCard(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()));

        if (isAdmin)
            throw new RejectOperationException("Операция отклонена. Администратор не может быть получателем карты");

        String cardNumber;
        do {
            cardNumber = generateCardNumber();
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
            throw new CardStatusException(REQUESTED_BLOCKED_MESSAGE);

        card.setStatus(CardStatus.BLOCKED);

        return CARD_BLOCKED_MESSAGE.formatted(mask(card.getCardNumber()));
    }


    public String activateCard(Long id) {
        var card = getCard(id);

        if (card.getStatus() == CardStatus.ACTIVE)
            throw new CardStatusException(CARD_ALREADY_ACTIVATED_MESSAGE);

        card.setStatus(CardStatus.ACTIVE);

        return CARD_SUCCESSFULLY_ACTIVATED_MESSAGE.formatted(mask(card.getCardNumber()));
    }


    public String deleteCard(Long id) {
        var card = getCard(id);

        cardRepository.delete(card);

        return CARD_DELETED_MESSAGE.formatted(mask(card.getCardNumber()));
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
                        new CardNotFoundException(CARD_NOT_FOUND_MESSAGE.formatted(id)));
    }


    private String generateCardNumber() {
        var sb = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            sb.append(RANDOM.nextInt(10));
        }

        return sb.toString();
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
