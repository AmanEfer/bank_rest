package com.example.bankcards.controller;

import com.example.bankcards.dto.user.PageUserResponseDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.dto.user.UserUpdateRequestDto;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final UserService userService;


    @GetMapping
    public PageUserResponseDto getAllUsers(Pageable pageable) {
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


    @PatchMapping("{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto dto
    ) {
        var updateUser = userService.updateUser(id, dto);

        return ResponseEntity.ok(updateUser);
    }


    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        var message = userService.deleteUser(id);

        return ResponseEntity.ok(message);
    }
}
