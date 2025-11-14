package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyRegisteredException extends BaseCustomException {

    public UserAlreadyRegisteredException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
