package com.example.bankcards.service.user;

import com.example.bankcards.dto.UserPageResponseDto;
import com.example.bankcards.dto.UserRegisterRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.dto.UserUpdateRequestDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void registerUser(UserRegisterRequestDto dto) {
        if (userRepository.existsByPhoneNumber(dto.phoneNumber()))
            throw new IllegalArgumentException("Пользователь с таким номером телефона уже зарегистрирован");

        var newUser = toUser(dto);
        userRepository.save(newUser);
    }

    public UserPageResponseDto getAllUsers(Pageable pageable) {
        Page<UserResponseDto> page = userRepository.findAll(pageable)
                .map(this::toUserResponseDto);

        return userPageResponseDto(page);
    }

    public UserResponseDto getUserById(Long id) {
        var user = getUser(id);

        return toUserResponseDto(user);
    }

    public UserResponseDto getUserByPhoneNumber(String phoneNumber) {
        var user = userRepository.getUserByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return toUserResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto dto) {
        var user = getUser(id);

        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());

        return toUserResponseDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new IllegalArgumentException("Пользователь не найден");

        userRepository.deleteById(id);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    private User toUser(UserRegisterRequestDto dto) {
        return User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .phoneNumber(dto.phoneNumber())
                .password(dto.password())
                .cards(new ArrayList<>())
                .build();
    }

    private UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    private UserPageResponseDto userPageResponseDto(Page<UserResponseDto> page) {
        return UserPageResponseDto.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .last(page.isLast())
                .build();
    }
}
