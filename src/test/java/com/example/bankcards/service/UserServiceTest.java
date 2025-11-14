package com.example.bankcards.service;

import com.example.bankcards.dto.user.PageUserResponseDto;
import com.example.bankcards.dto.user.UserRegisterRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.dto.user.UserUpdateRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserAlreadyRegisteredException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final String REGISTERED_SUCCESSFULLY_MESSAGE = "Пользователь Иванов Иван успешно зарегистрирован в системе";
    private static final String PHONE_NUMBER_ALREADY_EXISTS_MESSAGE = "Пользователь с таким номером телефона уже зарегистрирован";
    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь не найден";
    private static final String USER_DELETED_MESSAGE = "Пользователь с ID '%d' был удален";

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .phoneNumber("9261234567")
                .password(ENCODED_PASSWORD)
                .roles(Set.of(Role.builder().name(ROLE_USER).build()))
                .cards(new ArrayList<>())
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .phoneNumber("9261234567")
                .build();
    }


    @Test
    void registerUser_success() {
        var requestDto = UserRegisterRequestDto.builder()
                .firstName("Иван")
                .lastName("Иванов")
                .phoneNumber("9261234567")
                .password("qwerty12345!@#")
                .build();

        var role = Role.builder()
                .name(ROLE_USER)
                .build();

        when(userRepository.existsByPhoneNumber(requestDto.phoneNumber())).thenReturn(false);
        when(roleRepository.findByName(ROLE_USER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(requestDto.password())).thenReturn(ENCODED_PASSWORD);

        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> {
                    User u = i.getArgument(0);
                    u.setId(1L);
                    return u;
                });

        var result = userService.registerUser(requestDto);

        assertEquals(REGISTERED_SUCCESSFULLY_MESSAGE, result);

        verify(userRepository).existsByPhoneNumber(requestDto.phoneNumber());
        verify(roleRepository).findByName(ROLE_USER);
        verify(passwordEncoder).encode(requestDto.password());

        verify(userRepository).save(argThat(u ->
                u.getFirstName().equals(requestDto.firstName()) &&
                        u.getLastName().equals(requestDto.lastName()) &&
                        u.getPhoneNumber().equals(requestDto.phoneNumber()) &&
                        u.getPassword().equals(ENCODED_PASSWORD) &&
                        u.getCards() != null && u.getCards().isEmpty() &&
                        u.getRoles().stream()
                                .anyMatch(r -> r.getName().equals(ROLE_USER))
        ));
    }


    @Test
    void registerUser_phoneNumberAlreadyExists_throwsException() {
        var dto = UserRegisterRequestDto.builder()
                .phoneNumber(user.getPhoneNumber())
                .build();

        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(true);

        var ex = assertThrows(UserAlreadyRegisteredException.class,
                () -> userService.registerUser(dto));

        assertEquals(PHONE_NUMBER_ALREADY_EXISTS_MESSAGE, ex.getMessage());

        verify(userRepository).existsByPhoneNumber(anyString());

        verifyNoMoreInteractions(userRepository, roleRepository, passwordEncoder);
    }


    @Test
    void getAllUsers_success() {
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageUserResponseDto result = userService.getAllUsers(PageRequest.of(0, 3));
        UserResponseDto actualDto = result.content().get(0);

        assertAll(() -> {
            assertEquals(0, (int) result.page());
            assertEquals(1, (int) result.size());
            assertEquals(1, (int) result.totalPages());
            assertEquals(1, (long) result.totalElements());
            assertTrue(result.last());
        });

        assertAll(() -> {
            assertEquals(user.getId(), actualDto.id());
            assertEquals(user.getFirstName(), actualDto.firstName());
            assertEquals(user.getLastName(), actualDto.lastName());
            assertEquals(user.getPhoneNumber(), actualDto.phoneNumber());
        });
    }


    @Test
    void getUserById_success() {
        when((userRepository.findById(anyLong()))).thenReturn(Optional.of(user));

        assertEquals(userResponseDto, userService.getUserById(user.getId()));
    }


    @Test
    void getUserById_throwsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        var ex = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(25L));

        assertEquals(USER_NOT_FOUND_MESSAGE, ex.getMessage());
    }


    @Test
    void getUserByPhoneNumber_success() {
        when(userRepository.getUserByPhoneNumber(anyString()))
                .thenReturn(Optional.of(user));

        assertEquals(userResponseDto, userService.getUserByPhoneNumber(user.getPhoneNumber()));
    }


    @Test
    void getUserByPhoneNumber_throwsException() {
        when(userRepository.getUserByPhoneNumber(anyString()))
                .thenReturn(Optional.empty());

        var ex = assertThrows(UserNotFoundException.class,
                () -> userService.getUserByPhoneNumber(anyString()));

        assertEquals(USER_NOT_FOUND_MESSAGE, ex.getMessage());
    }


    @Test
    void updateUser_withCards() {
        var card1 = Card.builder()
                .id(1L)
                .placeholder(user.getFirstName() + " " + user.getLastName())
                .build();

        var card2 = Card.builder()
                .id(2L)
                .placeholder(user.getFirstName() + " " + user.getLastName())
                .build();

        user.setCards(List.of(card1, card2));

        var userUpdateDto = new UserUpdateRequestDto("Олег", "Тинькоф");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        var result = userService.updateUser(1L, userUpdateDto);

        assertEquals("Олег", result.firstName());
        assertEquals("Тинькоф", result.lastName());

        assertTrue(user.getCards().stream()
                .allMatch(card -> "Олег Тинькоф".equals(card.getPlaceholder()))
        );

        verify(cardRepository).saveAll(user.getCards());
    }


    @Test
    void updateUser_withoutCards() {
        var userUpdateDto = new UserUpdateRequestDto("Олег", "Тинькоф");

        user.setCards(null);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        var result = userService.updateUser(1L, userUpdateDto);

        assertEquals("Олег", result.firstName());
        assertEquals("Тинькоф", result.lastName());

        verifyNoMoreInteractions(cardRepository);
    }


    @Test
    void deleteUser_success() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        var result = userService.deleteUser(1L);

        assertEquals(USER_DELETED_MESSAGE.formatted(1L), result);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_throwsException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        var ex = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(1L));

        assertEquals(USER_NOT_FOUND_MESSAGE, ex.getMessage());

        verifyNoMoreInteractions(userRepository);
    }
}