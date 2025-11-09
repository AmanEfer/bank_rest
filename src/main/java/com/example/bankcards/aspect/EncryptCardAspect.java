package com.example.bankcards.aspect;

import com.example.bankcards.entity.Card;
import com.example.bankcards.util.Encryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class EncryptCardAspect {

    private final Encryptor encryptor;


    @Before("execution(* com.example.bankcards.repository.CardRepository.save*(..))")
    public void encryptBeforeSave(JoinPoint jp) {
        for (Object arg : jp.getArgs()) {
            if (arg instanceof Card card) {
                encryptCard(card);
            } else if (arg instanceof Iterable<?> items) {
                items.forEach(item -> {
                    if (item instanceof Card c)
                        encryptCard(c);
                });
            }
        }
    }


    @AfterReturning(pointcut = """
            execution(* com.example.bankcards.repository.CardRepository.find*(..))
            || execution(* com.example.bankcards.repository.CardRepository.get*(..))
            """,
            returning = "result")
    public void decryptAfterLoad(Object result) {
        if (result == null) return;

        if (result instanceof Card card) {
            decryptCard(card);

        } else if (result instanceof Optional<?> o
                && o.isPresent()
                && o.get() instanceof Card card) {
            decryptCard(card);

        } else if (result instanceof Page<?> page) {
            page.getContent().stream()
                    .filter(c -> c instanceof Card)
                    .map(obj -> (Card) obj)
                    .forEach(this::decryptCard);

        } else if (result instanceof Iterable<?> items) {
            items.forEach(item -> {
                if (item instanceof Card c)
                    decryptCard(c);
            });
        }
    }


    private void encryptCard(Card card) {
        if (card.getCardNumber() != null && !card.getCardNumber().isBlank()) {
            card.setEncryptedCardNumber(encryptor.encrypt(card.getCardNumber()));

            if (card.getLast4() == null || card.getLast4().isBlank()) {
                card.setLast4(card.getCardNumber().substring(card.getCardNumber().length() - 4));
            }
        }

        if (card.getPlaceholder() != null && !card.getPlaceholder().isBlank()) {
            card.setEncryptedPlaceholder(encryptor.encrypt(card.getPlaceholder()));
        }
    }


    private void decryptCard(Card card) {

        if (card.getEncryptedCardNumber() != null) {
            card.setCardNumber(encryptor.decrypt(card.getEncryptedCardNumber()));
        }

        if (card.getEncryptedPlaceholder() != null) {
            card.setPlaceholder(encryptor.decrypt(card.getEncryptedPlaceholder()));
        }
    }
}