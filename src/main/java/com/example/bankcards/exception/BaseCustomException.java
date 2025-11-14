package com.example.bankcards.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseCustomException extends RuntimeException {

    private final HttpStatus errorCode;

    public BaseCustomException(String message, HttpStatus errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
