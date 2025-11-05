package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsById(Long id);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> getUserByPhoneNumber(String phoneNumber);
}
