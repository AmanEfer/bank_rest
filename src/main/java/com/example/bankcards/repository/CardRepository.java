package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, Long> {

    boolean existsByCardNumber(String cardNumber);

    @Query("from Card c where c.user.id = :userId")
    Page<Card> getCardsByUserId(@Param("userId") Long userId, Pageable pageable);
}
