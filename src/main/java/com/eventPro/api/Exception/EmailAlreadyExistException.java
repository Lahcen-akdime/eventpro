package com.eventPro.api.Exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistException extends RuntimeException {
    public String field = "email" ;
    public EmailAlreadyExistException(String message) {
        super(message);
    }
}
