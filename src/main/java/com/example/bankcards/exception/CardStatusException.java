package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class CardStatusException extends BaseCustomException {


    public CardStatusException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
