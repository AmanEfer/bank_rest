package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class RejectOperationException extends BaseCustomException {

    public RejectOperationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
