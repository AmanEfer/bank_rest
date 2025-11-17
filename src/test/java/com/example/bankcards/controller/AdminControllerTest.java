package com.example.bankcards.controller;

import com.example.bankcards.dto.user.PageUserResponseDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.dto.user.UserUpdateRequestDto;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(
        controllers = AdminController.class,
        excludeAutoConfiguration = JpaBaseConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    private static final String URL = "/api/v1/admin/users";
    private static final long USER_ID = 1L;
    private static final String PHONE_NUMBER = "9261234567";
    private static final String FIRST_NAME = "Иван";
    private static final String LAST_NAME = "Иванов";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserResponseDto userResponseDto;
    private PageUserResponseDto pageUserResponseDto;


    @BeforeEach
    void setUp_200() {
        userResponseDto = UserResponseDto.builder()
                .id(USER_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .phoneNumber(PHONE_NUMBER)
                .build();

        pageUserResponseDto = PageUserResponseDto.builder()
                .content(List.of(userResponseDto))
                .page(0)
                .size(3)
                .totalPages(1)
                .totalElements(1L)
                .last(true)
                .build();
    }


    @Test
    void getAllUsers_200() throws Exception {
        var pageable = PageRequest.of(0, 3);

        when(userService.getAllUsers(any(Pageable.class)))
                .thenReturn(pageUserResponseDto);

        mockMvc.perform(get(URL)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        objectMapper.writeValueAsString(pageUserResponseDto)
                ));

        verify(userService).getAllUsers(pageable);
    }


    @Test
    void getUserById_200() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(userResponseDto);

        mockMvc.perform(get(URL + "/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        objectMapper.writeValueAsString(userResponseDto)
                ));

        verify(userService).getUserById(USER_ID);
    }


    @Test
    void getUserByPhoneNumber_200() throws Exception {
        when(userService.getUserByPhoneNumber(anyString())).thenReturn(userResponseDto);

        mockMvc.perform(get(URL + "/phone")
                        .param("phoneNumber", PHONE_NUMBER))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        objectMapper.writeValueAsString(userResponseDto)
                ));

        verify(userService).getUserByPhoneNumber(PHONE_NUMBER);
    }


    @Test
    void updateUser_200() throws Exception {
        var request = new UserUpdateRequestDto(FIRST_NAME, LAST_NAME);

        when(userService.updateUser(anyLong(), any(UserUpdateRequestDto.class)))
                .thenReturn(userResponseDto);

        mockMvc.perform(patch(URL + "/{id}", USER_ID)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        objectMapper.writeValueAsString(userResponseDto)
                ));

        userService.updateUser(USER_ID, request);
    }


    @Test
    void deleteUser_200() throws Exception {
        var message = "Пользователь с ID '%d' был удален";

        when(userService.deleteUser(anyLong())).thenReturn(message);

        mockMvc.perform(delete(URL + "/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().string(message));

        verify(userService).deleteUser(USER_ID);
    }
}