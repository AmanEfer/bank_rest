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
    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь не найден";
    private static final String CARD_BALANCE_MESSAGE = "Баланс карты: %s рублей";
    private static final String SUCCESS_TRANSFER_MESSAGE = "Совершен перевод %s рублей с карты %s на карту %s";
    private static final String CARD_ALREADY_BLOCKED_MESSAGE = "Карта уже была заблокирована ренее";
    private static final String CARD_EXPIRED_NOT_ACTIVE_MESSAGE = "Срок действия карты истек. Карта не активна";
    private static final String ALREADY_REQUESTED_BLOCKED_MESSAGE = "Вы уже запросили блокировку";
    private static final String UNKNOWN_STATUS_MESSAGE = "Неизвестный статус";
    private static final String REJECT_OPERATION_MESSAGE = "Операция отклонена. Карта заблокирована";
    private static final String CARD_EXPIRED_SUPPORT_MESSAGE = "Срок действия карты истек. Обратитесь в службу поддержки";
    private static final String SAME_CARD_TRANSFER_MESSAGE = "Вы пытаетесь перевести деньги на ту же карту, " +
            "с которой отправляете. \nВозможно, вы ошиблись.";
    private static final String DEPOSIT_MESSAGE = """
            На карту внесено %s рублей.
            Баланс карты составляет %s рублей
            """;
    private static final String WITHDRAW_MESSAGE = """
            С карты снято %s рублей.
            Баланс карты составляет %s рублей
            """;

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final StatusChanger statusChanger;


    public String showBalance(Long cardId, Long userId) {
        var cardBalance = cardRepository.getBalanceByCardIdAndUserId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundException(CARD_NOT_FOUND_MESSAGE));

        return CARD_BALANCE_MESSAGE.formatted(cardBalance.toString());
    }


    @Transactional
    public String deposit(Long userId, Long cardId, FundsDto fundsDto) {
        var card = getCardByCardIdAndUserId(cardId, userId);

        validCardStatus(cardId, card);

        card.setBalance(card.getBalance().add(fundsDto.funds())
                .setScale(2, RoundingMode.HALF_UP));
        card = cardRepository.saveAndFlush(card);

        return DEPOSIT_MESSAGE.formatted(fundsDto.funds().toString(), card.getBalance().toString());
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

        return WITHDRAW_MESSAGE.formatted(fundsDto.funds().toString(), card.getBalance().toString());
    }


    @Transactional
    public String transferToOwnCard(Long userId, TransferDto transferDto) {
        var fromCard = getCardByCardIdAndUserId(transferDto.from(), userId);
        var toCard = getCardByCardIdAndUserId(transferDto.to(), userId);

        validCardStatus(fromCard.getId(), fromCard);
        validCardStatus(toCard.getId(), toCard);

        if (fromCard.equals(toCard))
            throw new CardOperationFailedException(SAME_CARD_TRANSFER_MESSAGE);

        if (fromCard.getBalance().compareTo(transferDto.funds()) < 0)
            throw new CardOperationFailedException(INSUFFICIENT_FUNDS_MESSAGE);

        fromCard.setBalance(fromCard.getBalance().subtract(transferDto.funds())
                .setScale(2, RoundingMode.HALF_UP));

        toCard.setBalance(toCard.getBalance().add(transferDto.funds())
                .setScale(2, RoundingMode.HALF_UP));

        return SUCCESS_TRANSFER_MESSAGE.formatted(
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
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);

        Page<CardResponseDto> cards = cardRepository.findUserCards(pageable, userId, cardId, last4, status)
                .map(card -> toDto(card, userId));

        return toPageDto(cards);
    }


    @Transactional
    public ResponseCardBlockDto blockCard(Long userId, Long cardId, RequestCardBlockDto blockDto) {
        var card = getCardByCardIdAndUserId(cardId, userId);

        switch (card.getStatus()) {
            case ACTIVE -> card.setStatus(CardStatus.REQUESTED_BLOCKED);
            case BLOCKED -> throw new CardStatusException(CARD_ALREADY_BLOCKED_MESSAGE);
            case EXPIRED -> throw new CardStatusException(CARD_EXPIRED_NOT_ACTIVE_MESSAGE);
            case REQUESTED_BLOCKED -> throw new CardStatusException(ALREADY_REQUESTED_BLOCKED_MESSAGE);
            default -> throw new CardStatusException(UNKNOWN_STATUS_MESSAGE);
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
            throw new CardStatusException(REJECT_OPERATION_MESSAGE);
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

