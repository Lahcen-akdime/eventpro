package com.eventPro.api.Exception;

public class InvalidCredantialsException extends RuntimeException {
    public InvalidCredantialsException(String message) {
        super(message);
    }
}
