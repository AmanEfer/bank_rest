package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    boolean existsByCardNumber(String cardNumber);

    @Query("from Card c where c.user.id = :userId")
    Page<Card> findCardsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            select c.balance
            from Card c
            join c.user u
            where c.id = :cardId and u.id = :userId
            """)
    Optional<BigDecimal> getBalanceByCardIdAndUserId(@Param("cardId") Long cardId, @Param("userId") Long userId);

    @Query("""
            from Card c
            where c.id = :cardId and c.user.id = :userId
            """)
    Optional<Card> findCardByCardIdAndUserId(@Param("cardId") Long cardId, @Param("userId") Long userId);


    @Query("""
            from Card c
            where c.user.id = :userId
            and (:cardId is null or c.id = :cardId)
            and (:last4 is null or right(c.cardNumber, 4) = :last4)
            and (:status is null or c.status = :status)
            """)
    Page<Card> findUserCards(Pageable pageable,
                             @Param("userId") Long userId,
                             @Param("cardId") Long cardId,
                             @Param("last4") String last4,
                             @Param("status") CardStatus status);

    @Modifying
    @Query("""
            update Card c
            set c.status = :status
            where c.id = :id
            """)
    void updateCardByIdAndStatus(@Param("id") Long id, @Param("status") CardStatus status);
}
