package com.example.bankcards.controller;

import com.example.bankcards.dto.UserPageResponseDto;
import com.example.bankcards.dto.UserRegisterRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.dto.UserUpdateRequestDto;
import com.example.bankcards.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegisterRequestDto requestDto) {
        userService.registerUser(requestDto);

        return ResponseEntity.ok("Пользователь успешно зарегистрирован в системе");
    }

    @GetMapping
    public UserPageResponseDto getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @GetMapping("{id}")
    public UserResponseDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/phone")
    public UserResponseDto getUserByPhoneNumber(
            @RequestParam
            @Pattern(regexp = "^\\d{10}$",
                    message = "Поле с номером телефона должно состоять из 10 цифр без префикса")
            String phoneNumber
    ) {
        return userService.getUserByPhoneNumber(phoneNumber);
    }

    @PutMapping("{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto dto
    ) {
        var updateUser = userService.updateUser(id, dto);

        return ResponseEntity.ok(updateUser);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);

        return ResponseEntity.ok("Пользователь с ID '%d' был удален".formatted(id));
    }
}
