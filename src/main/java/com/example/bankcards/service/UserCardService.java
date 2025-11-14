package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.FundsDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.dto.card.RequestCardBlockDto;
import com.example.bankcards.dto.card.ResponseCardBlockDto;
import com.example.bankcards.dto.card.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationFailedException;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserCardService {

    private static final String INSUFFICIENT_FUNDS_MESSAGE = "Недостаточно средств на карте";
    private static final String CARD_NOT_FOUND_MESSAGE = "Карта не найдена";
    private static final String CARD_EXPIRED_SUPPORT_MESSAGE = "Срок действия карты истек. Обратитесь в службу поддержки";

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final StatusChanger statusChanger;


    public String showBalance(Long cardId, Long userId) {
        var cardBalance = cardRepository.getBalanceByCardIdAndUserId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundException(CARD_NOT_FOUND_MESSAGE));

        return "Баланс карты: %s рублей".formatted(cardBalance.toString());
    }


    @Transactional
    public String deposit(Long userId, Long cardId, FundsDto fundsDto) {
        var card = getCardByCardIdAndUserId(cardId, userId);

        validCardStatus(cardId, card);

        card.setBalance(card.getBalance().add(fundsDto.funds())
                .setScale(2, RoundingMode.HALF_UP));
        card = cardRepository.saveAndFlush(card);

        return """
                На карту внесено %s рублей.
                Баланс карты составляет %s рублей
                """
                .formatted(fundsDto.funds().toString(), card.getBalance().toString());
    }


    @Transactional
    public String withdraw(Long userId, Long cardId, FundsDto fundsDto) {
        var card = getCardByCardIdAndUserId(cardId, userId);

        validCardStatus(cardId, card);

        if (card.getBalance().compareTo(fundsDto.funds()) < 0)
            throw new CardOperationFailedException(INSUFFICIENT_FUNDS_MESSAGE);

        card.setBalance(card.getBalance().subtract(fundsDto.funds())
                .setScale(2, RoundingMode.HALF_UP));
        card = cardRepository.saveAndFlush(card);

        return """
                С карты снято %s рублей.
                Баланс карты составляет %s рублей
                """
                .formatted(fundsDto.funds().toString(), card.getBalance().toString());
    }


    @Transactional
    public String transferToOwnCard(Long userId, TransferDto transferDto) {
        var fromCard = getCardByCardIdAndUserId(transferDto.from(), userId);
        var toCard = getCardByCardIdAndUserId(transferDto.to(), userId);

        validCardStatus(fromCard.getId(), fromCard);
        validCardStatus(toCard.getId(), toCard);

        if (fromCard.equals(toCard))
            throw new CardOperationFailedException("Вы пытаетесь перевести деньги на ту же карту, " +
                    "с которой отправляете. \nВозможно, вы ошиблись.");

        if (fromCard.getBalance().compareTo(transferDto.funds()) < 0)
            throw new CardOperationFailedException(INSUFFICIENT_FUNDS_MESSAGE);

        fromCard.setBalance(fromCard.getBalance().subtract(transferDto.funds())
                .setScale(2, RoundingMode.HALF_UP));

        toCard.setBalance(toCard.getBalance().add(transferDto.funds())
                .setScale(2, RoundingMode.HALF_UP));

        return "Совершен перевод %s рублей с карты %s на карту %s".formatted(
                transferDto.funds(),
                mask(fromCard.getCardNumber()),
                mask(toCard.getCardNumber())
        );
    }


    public PageCardResponseDto searchUserCards(Pageable pageable,
                                               Long userId,
                                               Long cardId,
                                               String last4,
                                               CardStatus status) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь не найден");

        Page<CardResponseDto> cards = cardRepository.findUserCards(pageable, userId, cardId, last4, status)
                .map(card -> toDto(card, userId));

        return toPageDto(cards);
    }


    @Transactional
    public ResponseCardBlockDto blockCard(Long userId, Long cardId, RequestCardBlockDto blockDto) {
        var card = getCardByCardIdAndUserId(cardId, userId);

        CardStatus status = card.getStatus();
        switch (status) {
            case ACTIVE -> card.setStatus(CardStatus.REQUESTED_BLOCKED);
            case BLOCKED -> throw new CardStatusException("Карта уже была заблокирована ренее");
            case EXPIRED -> throw new CardStatusException("Срок действия карты истек. Карта не активна");
            case REQUESTED_BLOCKED -> throw new CardStatusException("Вы уже запросили блокировку");
            default -> throw new CardStatusException("Неизвестный статус");
        }

        return ResponseCardBlockDto.builder()
                .card(toDto(card, userId))
                .reason(blockDto.reason())
                .build();
    }


    private Card getCardByCardIdAndUserId(Long cardId, Long userId) {
        return cardRepository.findCardByCardIdAndUserId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundException(CARD_NOT_FOUND_MESSAGE));
    }


    private String mask(String cardNumber) {
        return CardMasker.maskCard(cardNumber);
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


    private void validCardStatus(Long cardId, Card card) {
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardStatusException("Операция отклонена. Карта заблокирована");
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardStatusException(CARD_EXPIRED_SUPPORT_MESSAGE);
        }

        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            statusChanger.changeStatus(cardId);
            throw new CardStatusException(CARD_EXPIRED_SUPPORT_MESSAGE);
        }
    }
}

