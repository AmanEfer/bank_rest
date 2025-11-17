package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.FundsDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.dto.card.RequestCardBlockDto;
import com.example.bankcards.dto.card.ResponseCardBlockDto;
import com.example.bankcards.dto.card.TransferDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(
        controllers = UserCardController.class,
        excludeAutoConfiguration = JpaBaseConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class UserCardControllerTest {

    private static final String URL = "/api/v1/users/cards";
    private static final long USER_ID = 1L;
    private static final long CARD_ID = 2L;
    private static final String HIDDEN_CARD_NUMBER = "**** **** **** 4444";
    private static final String LAST4 = "4444";
    private static final String PLACEHOLDER = "Иван Иванов";
    private static final String PHONE_NUMBER = "9265847312";
    private static final String PASSWORD = "encrypted_password";
    private static final CardStatus CARD_STATUS = CardStatus.ACTIVE;
    private static final BigDecimal BALANCE = BigDecimal.ZERO;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserCardService userCardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private CustomUserDetails customUserDetails;

    private CardResponseDto responseDto;
    private PageCardResponseDto pageCardResponseDto;
    private FundsDto fundsDto;
    private UsernamePasswordAuthenticationToken auth;


    @BeforeEach
    void setUp() {
        responseDto = CardResponseDto.builder()
                .id(CARD_ID)
                .cardNumber(HIDDEN_CARD_NUMBER)
                .placeholder(PLACEHOLDER)
                .expirationDate(LocalDate.now())
                .status(CARD_STATUS)
                .balance(BALANCE)
                .userId(USER_ID)
                .build();

        pageCardResponseDto = PageCardResponseDto.builder()
                .content(List.of(responseDto))
                .page(0)
                .size(3)
                .totalPages(1)
                .totalElements(1L)
                .last(true)
                .build();

        fundsDto = new FundsDto(new BigDecimal("5000.00"));

        customUserDetails = new CustomUserDetails(
                USER_ID,
                PHONE_NUMBER,
                PASSWORD,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        auth = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                customUserDetails.getPassword(),
                customUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }


    @Test
    void searchUserCards_200() throws Exception {
        var pageable = PageRequest.of(0, 3);

        when(userCardService.searchUserCards(any(Pageable.class), anyLong(),
                anyLong(), anyString(), any(CardStatus.class))
        ).thenReturn(pageCardResponseDto);

        mockMvc.perform(get(URL)
                        .with(user(customUserDetails))
                        .param("page", "0")
                        .param("size", "3")
                        .param("cardId", String.valueOf(CARD_ID))
                        .param("last4", LAST4)
                        .param("status", CARD_STATUS.name()))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        objectMapper.writeValueAsString(pageCardResponseDto)
                ));

        verify(userCardService).searchUserCards(pageable, USER_ID, CARD_ID, LAST4, CARD_STATUS);
    }


    @Test
    void checkBalance_200() throws Exception {
        var message = "Баланс карты: %s рублей".formatted(BALANCE);

        when(userCardService.showBalance(anyLong(), anyLong())).thenReturn(message);

        mockMvc.perform(get(URL + "/balance/{cardId}", CARD_ID)
                        .with(user(customUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().string(message));

        verify(userCardService).showBalance(CARD_ID, USER_ID);

    }


    @Test
    void deposit_200() throws Exception {
        var message = """
                На карту внесено %s рублей.
                Баланс карты составляет %s рублей
                """
                .formatted(fundsDto.funds().toString(), "5000.00");

        when(userCardService.deposit(anyLong(), anyLong(), any(FundsDto.class)))
                .thenReturn(message);

        mockMvc.perform(patch(URL + "/balance/{cardId}/deposit", CARD_ID)
                        .with(user(customUserDetails))
                        .content(objectMapper.writeValueAsString(fundsDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string(message));

        verify(userCardService).deposit(USER_ID, CARD_ID, fundsDto);
    }


    @Test
    void withdraw_200() throws Exception {
        var message = """
                С карты снято %s рублей.
                Баланс карты составляет %s рублей
                """
                .formatted(fundsDto.funds().toString(), "0.00");

        when(userCardService.withdraw(anyLong(), anyLong(), any(FundsDto.class)))
                .thenReturn(message);

        mockMvc.perform(patch(URL + "/balance/{cardId}/withdraw", CARD_ID)
                        .with(user(customUserDetails))
                        .content(objectMapper.writeValueAsString(fundsDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string(message));

        verify(userCardService).withdraw(USER_ID, CARD_ID, fundsDto);
    }


    @Test
    void transferToOwnCard_200() throws Exception {
        var transferDto = new TransferDto(CARD_ID, 3L, new BigDecimal("5000.00"));

        var message = "Совершен перевод %s рублей с карты %s на карту %s".formatted(
                transferDto.funds(),
                HIDDEN_CARD_NUMBER,
                "**** **** **** 5555");

        when(userCardService.transferToOwnCard(anyLong(), any(TransferDto.class)))
                .thenReturn(message);

        mockMvc.perform(patch(URL + "/transfer")
                        .with(user(customUserDetails))
                        .content(objectMapper.writeValueAsString(transferDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string(message));

        verify(userCardService).transferToOwnCard(USER_ID, transferDto);
    }


    @Test
    void requestCardBlock_200() throws Exception {
        var reason = "Данные карты похитили мошенники";
        var request = new RequestCardBlockDto(reason);
        var response = new ResponseCardBlockDto(responseDto, reason);

        when(userCardService.blockCard(anyLong(), anyLong(), any(RequestCardBlockDto.class)))
                .thenReturn(response);

        mockMvc.perform(patch(URL + "/cards/{cardId}/block", CARD_ID)
                        .with(user(customUserDetails))
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        objectMapper.writeValueAsString(response)
                ));

        verify(userCardService).blockCard(USER_ID, CARD_ID, request);
    }
}