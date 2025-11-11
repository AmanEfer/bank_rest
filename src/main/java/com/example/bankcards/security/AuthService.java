package com.example.bankcards.security;

import com.example.bankcards.dto.auth.AuthRequestDto;
import com.example.bankcards.dto.auth.AuthResponseDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.UserDetailsBuilder;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public AuthResponseDto authenticate(AuthRequestDto dto) {
        var user = getUser(dto.phoneNumber(), "Неправильно введен номер телефона или пароль");

        if (!passwordEncoder.matches(dto.password(), user.getPassword()))
            throw new IllegalArgumentException("Неправильно введен номер телефона или пароль");

        var accessToken = getAccessToken(user);
        var refreshToken = getRefreshToken(user);

        var roles = getRoles(user);

        return buildAuthResponseDto(accessToken, refreshToken, user, roles);
    }


    public AuthResponseDto refreshToken(String refreshToken) {
        var phoneNumber = jwtService.extractPhoneNumber(refreshToken, true);
        var user = getUser(phoneNumber, "Пользователь не найден");
        var userDetails = UserDetailsBuilder.buildUserDetails(user);

        if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
            throw new JwtException("Refresh токен не валиден");
        }

        var newAccess = getAccessToken(user);
        var newRefresh = getRefreshToken(user);
        var roles = getRoles(user);

        return buildAuthResponseDto(newAccess, newRefresh, user, roles);
    }


    private String getAccessToken(User user) {
        return jwtService.generateAccessToken(user);
    }


    private String getRefreshToken(User user) {
        return jwtService.generateRefreshToken(user);
    }


    private User getUser(String phoneNumber, String exceptionMessage) {
        return userRepository.getUserByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(exceptionMessage));
    }


    private Set<String> getRoles(User user) {
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }


    private AuthResponseDto buildAuthResponseDto(
            String accessToken, String refreshToken, User user, Set<String> roles
    ) {
        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }
}
