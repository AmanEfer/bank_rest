package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.exception.RejectOperationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.Encryptor;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceTest {

    private static final long USER_ID = 1L;
    private static final long WRONG_USER_ID = 0L;
    private static final long CARD_ID = 1L;
    private static final long WRONG_CARD_ID = 0L;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Encryptor encryptor;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @InjectMocks
    private AdminCardService adminCardService;

    private User user;
    private Card card;
    private CardResponseDto expectedCardResponse;
    private final String cardNumber = "1111222233334444";
    private final String encryptedNumber = "encoded_card_number";


    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .firstName("Иван")
                .lastName("Иванов")
                .phoneNumber("9261234567")
                .password("encoded_password")
                .roles(new HashSet<>(Set.of(Role.builder().name("ROLE_USER").build())))
                .cards(new ArrayList<>())
                .build();

        card = Card.builder()
                .id(CARD_ID)
                .cardNumber(cardNumber)
                .last4(cardNumber.substring(cardNumber.length() - 4))
                .placeholder(user.getFirstName() + " " + user.getLastName())
                .expirationDate(LocalDate.now().plusYears(10))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .user(user)
                .build();

        expectedCardResponse = CardResponseDto.builder()
                .id(CARD_ID)
                .cardNumber("**** **** **** " + card.getLast4())
                .placeholder(card.getPlaceholder())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .userId(USER_ID)
                .build();
    }


    @Test
    void createNewCard_success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(cardNumberGenerator.generate()).thenReturn(cardNumber);
        when(encryptor.encrypt(anyString())).thenReturn(encryptedNumber);
        when(cardRepository.existsByEncryptedCardNumber(anyString())).thenReturn(false);

        when(cardRepository.saveAndFlush(any(Card.class)))
                .thenAnswer(i -> {
                    Card c = i.getArgument(0);
                    c.setId(CARD_ID);
                    return c;
                });

        var result = adminCardService.createNewCard(USER_ID);

        assertEquals(expectedCardResponse, result);

        verify(userRepository).findById(USER_ID);
        verify(cardNumberGenerator).generate();
        verify(encryptor).encrypt(cardNumber);
        verify(cardRepository).existsByEncryptedCardNumber(encryptedNumber);

        verify(cardRepository).saveAndFlush(argThat(i ->
                i.getCardNumber().equals(cardNumber) &&
                        i.getLast4().equals(card.getLast4()) &&
                        i.getPlaceholder().equals(card.getPlaceholder()) &&
                        i.getExpirationDate().equals(card.getExpirationDate()) &&
                        i.getStatus() == CardStatus.ACTIVE &&
                        i.getBalance().compareTo(card.getBalance()) == 0 &&
                        i.getUser().equals(card.getUser())
        ));
    }


    @Test
    void createNewCard_userNotFound_throwsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        var ex = assertThrows(UserNotFoundException.class,
                () -> adminCardService.createNewCard(WRONG_USER_ID));

        assertEquals("Пользователь не найден", ex.getMessage());

        verifyNoMoreInteractions(cardNumberGenerator, encryptor, cardRepository);
    }


    @Test
    void createNewCard_userIsAdmin_throwsException() {
        user.getRoles().add(Role.builder().name("ROLE_ADMIN").build());

        when(userRepository.findById(anyLong())).thenReturn((Optional.of(user)));

        var ex = assertThrows(RejectOperationException.class,
                () -> adminCardService.createNewCard(USER_ID));

        assertEquals("Операция отклонена. Администратор не может быть получателем карты", ex.getMessage());

        verify(userRepository).findById(USER_ID);

        verifyNoMoreInteractions(cardNumberGenerator, cardRepository, encryptor);
    }


    @Test
    void blockCard_success() {
        card.setStatus(CardStatus.REQUESTED_BLOCKED);

        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

        var result = adminCardService.blockCard(CARD_ID);

        assertEquals("Карта **** **** **** 4444 заблокирована", result);

        verify(cardRepository).findById(CARD_ID);
    }


    @Test
    void blockCard_cardNotFound_throwsException() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());

        var ex = assertThrows(CardNotFoundException.class,
                () -> adminCardService.blockCard(WRONG_CARD_ID));

        assertEquals("Карта с ID 0 не найдена", ex.getMessage());

        verify(cardRepository).findById(WRONG_CARD_ID);
    }


    @Test
    void blockCard_unsuitableStatus_throwsException() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

        var ex = assertThrows(CardStatusException.class,
                () -> adminCardService.blockCard(CARD_ID));

        assertEquals("Для блокировки карты необходимо отправить запрос на блокировку", ex.getMessage());

        verify(cardRepository).findById(CARD_ID);
    }


    @Test
    void activateCard_success() {
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

        var result = adminCardService.activateCard(CARD_ID);

        assertEquals("Карта **** **** **** 4444 активирована", result);

        verify(cardRepository).findById(CARD_ID);
    }


    @Test
    void activateCard_alreadyActivated_throwsException() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

        var ex = assertThrows(CardStatusException.class,
                () -> adminCardService.activateCard(CARD_ID));

        assertEquals("Карта уже активирована", ex.getMessage());

        verify(cardRepository).findById(CARD_ID);
    }


    @Test
    void deleteCard_success() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));
        doNothing().when(cardRepository).delete(any(Card.class));

        var result = adminCardService.deleteCard(CARD_ID);

        assertEquals("Карта **** **** **** 4444 удалена", result);

        verify(cardRepository).findById(CARD_ID);
        verify(cardRepository).delete(eq(card));
    }


    @Test
    void getAllCards_success() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Card> page = new PageImpl<>(List.of(card), pageable, 1);

        var expectedPageResponse = PageCardResponseDto.builder()
                .content(List.of(expectedCardResponse))
                .page(0)
                .size(3)
                .totalPages(1)
                .totalElements(1L)
                .last(true)
                .build();

        when(cardRepository.findAll(any(Pageable.class))).thenReturn(page);

        var result = adminCardService.getAllCards(pageable);

        assertEquals(expectedPageResponse, result);

        verify(cardRepository).findAll(pageable);
    }


    @Test
    void getUserCards_success() {
        var pageable = PageRequest.of(0, 3);
        var page = new PageImpl<>(List.of(card), pageable, 1);

        var expectedPageResponse = PageCardResponseDto.builder()
                .content(List.of(expectedCardResponse))
                .page(0)
                .size(3)
                .totalPages(1)
                .totalElements(1L)
                .last(true)
                .build();

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(cardRepository.findCardsByUserId(anyLong(), any(Pageable.class)))
                .thenReturn(page);

        var result = adminCardService.getUserCards(USER_ID, pageable);

        assertEquals(expectedPageResponse, result);

        verify(userRepository).existsById(USER_ID);
        verify(cardRepository).findCardsByUserId(USER_ID, pageable);
    }


    @Test
    void getUserCards_userNotFound_throwsException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        var ex = assertThrows(UserNotFoundException.class,
                () -> adminCardService.getUserCards(WRONG_USER_ID, Pageable.unpaged()));

        assertEquals("Пользователь не найден", ex.getMessage());

        verifyNoMoreInteractions(cardRepository);
    }
}