package com.eventPro.api.Exception;

public class EventAlreadyPublishedException extends RuntimeException {
    public EventAlreadyPublishedException(String message) {
        super(message);
    }
}
