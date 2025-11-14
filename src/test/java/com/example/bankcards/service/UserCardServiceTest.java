package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.FundsDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.dto.card.RequestCardBlockDto;
import com.example.bankcards.dto.card.ResponseCardBlockDto;
import com.example.bankcards.dto.card.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationFailedException;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCardServiceTest {

    private static final long USER_ID = 1L;
    private static final long WRONG_USER_ID = 0L;
    private static final long CARD_ID = 1L;
    private static final long WRONG_CARD_ID = 0L;
    private static final long TO_CARD_ID = 2L;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatusChanger statusChanger;

    @InjectMocks
    private UserCardService userCardService;

    private User user;
    private Card card;
    private CardResponseDto cardResponse;
    private ResponseCardBlockDto responseCardBlock;
    private RequestCardBlockDto requestCardBlock;


    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .firstName("Иван")
                .lastName("Иванов")
                .phoneNumber("9261234567")
                .password("encoded_password")
                .roles(Set.of(Role.builder().name("ROLE_USER").build()))
                .cards(new ArrayList<>())
                .build();

        card = Card.builder()
                .id(CARD_ID)
                .last4("7221")
                .expirationDate(LocalDate.now().plusYears(10))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("35000.00"))
                .user(user)
                .cardNumber("2502551189197221")
                .placeholder("Иван Иванов")
                .build();

        cardResponse = CardResponseDto.builder()
                .id(CARD_ID)
                .cardNumber("**** **** **** 7221")
                .placeholder("Иван Иванов")
                .expirationDate(card.getExpirationDate())
                .status(CardStatus.REQUESTED_BLOCKED)
                .balance(new BigDecimal("35000.00"))
                .userId(USER_ID)
                .build();

        responseCardBlock = ResponseCardBlockDto.builder()
                .card(cardResponse)
                .reason("Данные карты похитили мошенники")
                .build();

        requestCardBlock = new RequestCardBlockDto("Данные карты похитили мошенники");
    }


    @Test
    void showBalance_success() {
        var balance = new BigDecimal("5000.00");

        when(cardRepository.getBalanceByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(balance));

        var result = userCardService.showBalance(CARD_ID, USER_ID);

        assertEquals("Баланс карты: 5000.00 рублей", result);

        verify(cardRepository).getBalanceByCardIdAndUserId(CARD_ID, USER_ID);
    }


    @Test
    void showBalance_cardNotFound_throwsException() {
        when(cardRepository.getBalanceByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        var ex = assertThrows(CardNotFoundException.class,
                () -> userCardService.showBalance(WRONG_CARD_ID, USER_ID));

        assertEquals("Карта не найдена", ex.getMessage());

        verify(cardRepository).getBalanceByCardIdAndUserId(WRONG_CARD_ID, USER_ID);
    }


    @Test
    void deposit_success() {
        var funds = new FundsDto(new BigDecimal("5000.00"));

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));

        when(cardRepository.saveAndFlush(any(Card.class))).thenReturn(card);

        var result = userCardService.deposit(USER_ID, CARD_ID, funds);

        assertEquals("""
                        На карту внесено 5000.00 рублей.
                        Баланс карты составляет 40000.00 рублей
                        """,
                result);

        assertEquals(0, new BigDecimal("40000.00").compareTo(card.getBalance()));

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
        verify(cardRepository).saveAndFlush(card);

        verifyNoMoreInteractions(statusChanger);
    }


    @Test
    void deposit_cardNotFound_throwsException() {
        var funds = new FundsDto(new BigDecimal("5000.00"));

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        var ex = assertThrows(CardNotFoundException.class,
                () -> userCardService.deposit(USER_ID, WRONG_CARD_ID, funds));

        assertEquals("Карта не найдена", ex.getMessage());
    }


    @Test
    void deposit_blocked_throwsException() {
        var funds = new FundsDto(new BigDecimal("5000.00"));
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));


        var ex = assertThrows(CardStatusException.class,
                () -> userCardService.deposit(USER_ID, CARD_ID, funds));

        assertEquals("Операция отклонена. Карта заблокирована", ex.getMessage());

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
    }


    @Test
    void deposit_wasExpired_throwsException() {
        var funds = new FundsDto(new BigDecimal("5000.00"));
        card.setStatus(CardStatus.EXPIRED);

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));


        var ex = assertThrows(CardStatusException.class,
                () -> userCardService.deposit(USER_ID, CARD_ID, funds));

        assertEquals("Срок действия карты истек. Обратитесь в службу поддержки", ex.getMessage());

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
    }


    @Test
    void deposit_changeToExpired_throwsException() {
        var funds = new FundsDto(new BigDecimal("5000.00"));
        card.setExpirationDate(LocalDate.now().minusMonths(1));

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));

        doAnswer(i -> {
            card.setStatus(CardStatus.EXPIRED);
            return null;
        })
                .when(statusChanger).changeStatus(eq(CARD_ID));

        var ex = assertThrows(CardStatusException.class,
                () -> userCardService.deposit(USER_ID, CARD_ID, funds));

        assertEquals("Срок действия карты истек. Обратитесь в службу поддержки", ex.getMessage());

        assertSame(CardStatus.EXPIRED, card.getStatus());

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
        verify(statusChanger).changeStatus(CARD_ID);
    }


    @Test
    void withdraw_success() {
        var funds = new FundsDto(new BigDecimal("5000.00"));

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));

        when(cardRepository.saveAndFlush(any(Card.class))).thenReturn(card);

        var result = userCardService.withdraw(USER_ID, CARD_ID, funds);

        assertEquals("""
                        С карты снято 5000.00 рублей.
                        Баланс карты составляет 30000.00 рублей
                        """,
                result);

        assertEquals(0, new BigDecimal("30000.00").compareTo(card.getBalance()));

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
        verify(cardRepository).saveAndFlush(card);

        verifyNoMoreInteractions(statusChanger);
    }


    @Test
    void withdraw_insufficientFunds_throwException() {
        var funds = new FundsDto(new BigDecimal("50000.00"));

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));

        var ex = assertThrows(CardOperationFailedException.class,
                () -> userCardService.withdraw(USER_ID, CARD_ID, funds));

        assertEquals("Недостаточно средств на карте", ex.getMessage());
        assertEquals(0, new BigDecimal("35000.00").compareTo(card.getBalance()));

        verify(cardRepository).findCardByCardIdAndUserId(USER_ID, CARD_ID);

        verifyNoMoreInteractions(statusChanger, cardRepository);
    }


    @Test
    void transferToOwnCard_success() {
        var transferDto = new TransferDto(CARD_ID, TO_CARD_ID, new BigDecimal("7000.00"));

        var toCard = Card.builder()
                .id(TO_CARD_ID)
                .last4("1566")
                .expirationDate(LocalDate.now().plusYears(5))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .user(user)
                .cardNumber("5818215397521566")
                .placeholder("Иван Иванов")
                .build();

        when(cardRepository.findCardByCardIdAndUserId(eq(CARD_ID), anyLong()))
                .thenReturn(Optional.of(card));

        when(cardRepository.findCardByCardIdAndUserId(eq(TO_CARD_ID), anyLong()))
                .thenReturn(Optional.of(toCard));

        var result = userCardService.transferToOwnCard(USER_ID, transferDto);

        assertEquals("Совершен перевод 7000.00 рублей с карты **** **** **** 7221 на карту **** **** **** 1566",
                result);

        assertEquals(0, new BigDecimal("28000.00").compareTo(card.getBalance()));
        assertEquals(0, new BigDecimal("8000.00").compareTo(toCard.getBalance()));

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
        verify(cardRepository).findCardByCardIdAndUserId(TO_CARD_ID, USER_ID);

        verifyNoMoreInteractions(statusChanger);
    }


    @Test
    void transferToOwnCard_sameCard_throwsException() {
        var transferDto = new TransferDto(CARD_ID, CARD_ID, new BigDecimal("7000.00"));

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));

        var ex = assertThrows(CardOperationFailedException.class,
                () -> userCardService.transferToOwnCard(USER_ID, transferDto));

        assertEquals("Вы пытаетесь перевести деньги на ту же карту, с которой отправляете. \nВозможно, вы ошиблись.",
                ex.getMessage());

        verify(cardRepository, times(2)).findCardByCardIdAndUserId(CARD_ID, USER_ID);

        verifyNoMoreInteractions(statusChanger);
    }


    @Test
    void transferToOwnCard_insufficientFunds_throwsException() {
        var transferDto = new TransferDto(CARD_ID, TO_CARD_ID, new BigDecimal("7000.00"));

        var toCard = Card.builder()
                .id(TO_CARD_ID)
                .last4("1566")
                .expirationDate(LocalDate.now().plusYears(5))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .user(user)
                .cardNumber("5818215397521566")
                .placeholder("Иван Иванов")
                .build();

        card.setBalance(BigDecimal.ONE);

        when(cardRepository.findCardByCardIdAndUserId(eq(CARD_ID), anyLong()))
                .thenReturn(Optional.of(card));

        when(cardRepository.findCardByCardIdAndUserId(eq(TO_CARD_ID), anyLong()))
                .thenReturn(Optional.of(toCard));

        var ex = assertThrows(CardOperationFailedException.class,
                () -> userCardService.transferToOwnCard(USER_ID, transferDto));

        assertEquals("Недостаточно средств на карте", ex.getMessage());
        assertEquals(0, BigDecimal.ONE.compareTo(card.getBalance()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(toCard.getBalance()));

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
        verify(cardRepository).findCardByCardIdAndUserId(TO_CARD_ID, USER_ID);

        verifyNoMoreInteractions(statusChanger);
    }


    @Test
    void searchUserCards_success() {
        var pageable = PageRequest.of(0, 3);
        var page = new PageImpl<>(List.of(card), pageable, 1);

        var cardResponse = CardResponseDto.builder()
                .id(CARD_ID)
                .cardNumber("**** **** **** 7221")
                .placeholder("Иван Иванов")
                .expirationDate(card.getExpirationDate())
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("35000.00"))
                .userId(USER_ID)
                .build();

        var pageCardResponse = PageCardResponseDto.builder()
                .content(List.of(cardResponse))
                .page(0)
                .size(3)
                .totalPages(1)
                .totalElements(1L)
                .last(true)
                .build();

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(cardRepository.findUserCards(
                any(Pageable.class), anyLong(), anyLong(), anyString(), any(CardStatus.class))
        ).thenReturn(page);

        var result = userCardService.searchUserCards(
                pageable, USER_ID, CARD_ID, card.getLast4(), CardStatus.ACTIVE
        );

        assertEquals(pageCardResponse, result);

        verify(userRepository).existsById(USER_ID);
        verify(cardRepository).findUserCards(pageable, USER_ID, CARD_ID, card.getLast4(), card.getStatus());
    }


    @Test
    void searchUserCards_userNotFound_throwsException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        var ex = assertThrows(UserNotFoundException.class,
                () -> userCardService.searchUserCards(
                        Pageable.unpaged(), WRONG_USER_ID, null, null, null)
        );

        assertEquals("Пользователь не найден", ex.getMessage());

        verify(userRepository).existsById(WRONG_USER_ID);

        verifyNoMoreInteractions(cardRepository);
    }


    @Test
    void maskCard_success_hiddenCard() {
        Page<Card> page = new PageImpl<>(List.of(card));

        when(userRepository.existsById(any())).thenReturn(true);
        when(cardRepository.findUserCards(any(), any(), any(), any(), any()))
                .thenReturn(page);

        var result = userCardService.searchUserCards(null, null, null, null, null);

        assertEquals("**** **** **** 7221", result.content().get(0).cardNumber());
    }


    @Test
    void maskCard_success_fullHiddenCard() {
        card.setCardNumber("12345");
        Page<Card> page = new PageImpl<>(List.of(card));

        when(userRepository.existsById(any())).thenReturn(true);
        when(cardRepository.findUserCards(any(), any(), any(), any(), any()))
                .thenReturn(page);

        var result = userCardService.searchUserCards(null, null, null, null, null);

        assertEquals("**** **** **** ****", result.content().get(0).cardNumber());
    }


    @Test
    void maskCard_cardNumberIsNull_throwsException() {
        card.setCardNumber(null);
        Page<Card> page = new PageImpl<>(List.of(card));

        when(userRepository.existsById(any())).thenReturn(true);
        when(cardRepository.findUserCards(any(), any(), any(), any(), any()))
                .thenReturn(page);

        var ex = assertThrows(IllegalArgumentException.class,
                () -> userCardService.searchUserCards(null, null, null, null, null));


        assertEquals("Неверное значение строки cardNumber: null или пустая строка", ex.getMessage());
    }


    @Test
    void blockCard_success() {
        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));

        var result = userCardService.blockCard(USER_ID, CARD_ID, requestCardBlock);

        assertEquals(responseCardBlock, result);

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
    }


    @Test
    void blockCard_cardBlocked_throwsException() {
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));

        var ex = assertThrows(CardStatusException.class,
                () -> userCardService.blockCard(USER_ID, CARD_ID, requestCardBlock));

        assertEquals("Карта уже была заблокирована ренее", ex.getMessage());

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
    }


    @Test
    void blockCard_cardExpired_throwsException() {
        card.setStatus(CardStatus.EXPIRED);

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));

        var ex = assertThrows(CardStatusException.class,
                () -> userCardService.blockCard(USER_ID, CARD_ID, requestCardBlock));

        assertEquals("Срок действия карты истек. Карта не активна", ex.getMessage());

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
    }


    @Test
    void blockCard_cardRequestedBlocked_throwsException() {
        card.setStatus(CardStatus.REQUESTED_BLOCKED);

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));

        var ex = assertThrows(CardStatusException.class,
                () -> userCardService.blockCard(USER_ID, CARD_ID, requestCardBlock));

        assertEquals("Вы уже запросили блокировку", ex.getMessage());

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
    }

    @Test
    void blockCard_cardUnknownStatus_throwsException() {
        card.setStatus(CardStatus.UNKNOWN);

        when(cardRepository.findCardByCardIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(card));

        var ex = assertThrows(CardStatusException.class,
                () -> userCardService.blockCard(USER_ID, CARD_ID, requestCardBlock));

        assertEquals("Неизвестный статус", ex.getMessage());

        verify(cardRepository).findCardByCardIdAndUserId(CARD_ID, USER_ID);
    }
}