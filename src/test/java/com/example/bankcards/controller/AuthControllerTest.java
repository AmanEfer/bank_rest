package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.AuthRequestDto;
import com.example.bankcards.dto.auth.AuthResponseDto;
import com.example.bankcards.dto.user.UserRegisterRequestDto;
import com.example.bankcards.security.AuthService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = JpaBaseConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private static final String URL = "/api/v1/auth";
    private static final String FIRST_NAME = "Иван";
    private static final String LAST_NAME = "Иванов";
    private static final String PHONE_NUMBER = "9261234567";
    private static final String PASSWORD = "parol12345";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzd...";
    private static final String REFRESH_TOKEN = "aGVsbG8gdGhlIHJlZnJlc2ggdG9rZW4gaXMgYSBsb25nZXIgY29kZSBvZiBjaGFyYWN0ZXJz";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserRegisterRequestDto registerRequestDto;
    private AuthRequestDto authRequestDto;
    private AuthResponseDto authResponseDto;

    @BeforeEach
    void setUp() {
        registerRequestDto = UserRegisterRequestDto.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .phoneNumber(PHONE_NUMBER)
                .password(PASSWORD)
                .build();

        authRequestDto = new AuthRequestDto(PHONE_NUMBER, PASSWORD);

        authResponseDto = AuthResponseDto.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .roles(Set.of(ROLE_USER))
                .build();
    }


    @Test
    void registerUser_200() throws Exception {
        var message = "Пользователь %s %s успешно зарегистрирован в системе"
                .formatted(LAST_NAME, FIRST_NAME);

        when(userService.registerUser(any(UserRegisterRequestDto.class)))
                .thenReturn(message);

        mockMvc.perform(post(URL + "/register")
                        .content(objectMapper.writeValueAsString(registerRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string(message));

        verify(userService).registerUser(registerRequestDto);
    }


    @Test
    void login_200() throws Exception {
        when(authService.authenticate(any(AuthRequestDto.class)))
                .thenReturn(authResponseDto);

        mockMvc.perform(post(URL + "/login")
                        .content(objectMapper.writeValueAsString(registerRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(authResponseDto)));

        verify(authService).authenticate(authRequestDto);
    }


    @Test
    void refresh_200() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", REFRESH_TOKEN);

        var response = AuthResponseDto.builder()
                .accessToken("new_access_token")
                .refreshToken("new_refresh_token")
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .roles(Set.of(ROLE_USER))
                .build();

        when(authService.refreshToken(anyString())).thenReturn(response);

        mockMvc.perform(post(URL + "/refresh")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(response))
                );

        verify(authService).refreshToken(REFRESH_TOKEN);
    }
}