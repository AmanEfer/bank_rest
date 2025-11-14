package com.example.bankcards.service;

import com.example.bankcards.dto.user.PageUserResponseDto;
import com.example.bankcards.dto.user.UserRegisterRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.dto.user.UserUpdateRequestDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserAlreadyRegisteredException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь не найден";
    private static final String PHONE_NUMBER_ALREADY_EXISTS_MESSAGE = "Пользователь с таким номером телефона уже зарегистрирован";
    private static final String REGISTERED_SUCCESSFULLY_MESSAGE = "Пользователь %s %s успешно зарегистрирован в системе";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String USER_DELETED_MESSAGE = "Пользователь с ID '%d' был удален";

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public String registerUser(UserRegisterRequestDto dto) {
        if (userRepository.existsByPhoneNumber(dto.phoneNumber()))
            throw new UserAlreadyRegisteredException(PHONE_NUMBER_ALREADY_EXISTS_MESSAGE);

        var newUser = toUser(dto);
        userRepository.save(newUser);

        return REGISTERED_SUCCESSFULLY_MESSAGE.formatted(newUser.getLastName(), newUser.getFirstName());
    }


    public PageUserResponseDto getAllUsers(Pageable pageable) {
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
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE));

        return toUserResponseDto(user);
    }


    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto dto) {
        var user = getUser(id);

        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());

        if (user.getCards() != null) {
            user.getCards().forEach(card ->
                    card.setPlaceholder(user.getFirstName() + " " + user.getLastName()));

            cardRepository.saveAll(user.getCards());
        }

        return toUserResponseDto(user);
    }


    @Transactional
    public String deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);

        userRepository.deleteById(id);

        return USER_DELETED_MESSAGE.formatted(id);
    }


    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE));
    }


    private User toUser(UserRegisterRequestDto dto) {
        var role = roleRepository.findByName(ROLE_USER)
                .orElseGet(() -> Role.builder().name(ROLE_USER).build());

        return User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .phoneNumber(dto.phoneNumber())
                .password(passwordEncoder.encode(dto.password()))
                .roles(Set.of(role))
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


    private PageUserResponseDto userPageResponseDto(Page<UserResponseDto> page) {
        return PageUserResponseDto.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .last(page.isLast())
                .build();
    }
}
