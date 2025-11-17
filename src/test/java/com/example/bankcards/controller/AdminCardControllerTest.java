package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.PageCardResponseDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AdminCardService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(
        controllers = AdminCardController.class,
        excludeAutoConfiguration = {JpaBaseConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class AdminCardControllerTest {

    private static final String URL = "/api/v1/admin/cards";
    private static final long USER_ID = 1L;
    private static final long CARD_ID = 2L;
    private static final String HIDDEN_CARD_NUMBER = "**** **** **** 4444";
    private static final String PLACEHOLDER = "Иван Иванов";


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminCardService adminCardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private CardResponseDto responseDto;
    private PageCardResponseDto pageCardResponseDto;


    @BeforeEach
    void setUp() {
        responseDto = CardResponseDto.builder()
                .id(CARD_ID)
                .cardNumber(HIDDEN_CARD_NUMBER)
                .placeholder(PLACEHOLDER)
                .expirationDate(LocalDate.now())
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
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
    }


    @Test
    void issueNewCard_200() throws Exception {
        when(adminCardService.createNewCard(anyLong())).thenReturn(responseDto);

        mockMvc.perform(post(URL)
                        .param("userId", String.valueOf(CARD_ID)))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(responseDto)));

        verify(adminCardService).createNewCard(CARD_ID);
    }


    @Test
    void blockCard_200() throws Exception {
        var message = "Карта %s заблокирована".formatted(HIDDEN_CARD_NUMBER);

        when(adminCardService.blockCard(anyLong())).thenReturn(message);

        mockMvc.perform(patch(URL + "/{id}/block", CARD_ID))
                .andExpect(status().isOk())
                .andExpect(content().string(message));

        verify(adminCardService).blockCard(CARD_ID);
    }


    @Test
    void activateCard_200() throws Exception {
        var message = "Карта %s активирована".formatted(HIDDEN_CARD_NUMBER);

        when(adminCardService.activateCard(anyLong())).thenReturn(message);

        mockMvc.perform(patch(URL + "/{id}/activate", CARD_ID))
                .andExpect(status().isOk())
                .andExpect(content().string(message));

        verify(adminCardService).activateCard(CARD_ID);
    }


    @Test
    void deleteCard_200() throws Exception {
        var message = "Карта %s удалена".formatted(HIDDEN_CARD_NUMBER);

        when(adminCardService.deleteCard(anyLong())).thenReturn(message);

        mockMvc.perform(delete(URL + "/{id}", CARD_ID))
                .andExpect(status().isOk())
                .andExpect(content().string(message));

        verify(adminCardService).deleteCard(CARD_ID);
    }


    @Test
    void viewAllCards_200() throws Exception {
        var pageable = PageRequest.of(0, 3);

        when(adminCardService.getAllCards(any(Pageable.class)))
                .thenReturn(pageCardResponseDto);

        mockMvc.perform(get(URL)
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        objectMapper.writeValueAsString(pageCardResponseDto))
                );

        verify(adminCardService).getAllCards(pageable);
    }


    @Test
    void viewUserCards_200() throws Exception {
        var pageable = PageRequest.of(0, 3);

        when(adminCardService.getUserCards(anyLong(), any(Pageable.class)))
                .thenReturn(pageCardResponseDto);

        mockMvc.perform(get(URL + "/user/{id}", USER_ID)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        objectMapper.writeValueAsString(pageCardResponseDto)
                ));

        verify(adminCardService).getUserCards(USER_ID, pageable);
    }
}