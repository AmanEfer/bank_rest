package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class CardOperationFailedException extends BaseCustomException {

    public CardOperationFailedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
