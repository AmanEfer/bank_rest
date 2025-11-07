package com.example.bankcards.service;

import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatusChanger {

    private final CardRepository cardRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeStatus(Long cardId) {
        cardRepository.updateCardByIdAndStatus(cardId, CardStatus.EXPIRED);
    }
}
