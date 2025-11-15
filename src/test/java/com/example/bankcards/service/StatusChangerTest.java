package com.example.bankcards.service;

import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class StatusChangerTest {

    private static final long CARD_ID = 1L;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private StatusChanger statusChanger;

    @Test
    void changeStatus_success() {
        statusChanger.changeStatus(CARD_ID);

        verify(cardRepository).updateCardByIdAndStatus(CARD_ID, CardStatus.EXPIRED);

        verifyNoMoreInteractions(cardRepository);
    }
}