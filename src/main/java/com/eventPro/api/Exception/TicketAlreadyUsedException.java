package com.eventPro.api.Exception;

public class TicketAlreadyUsedException extends RuntimeException {
    public TicketAlreadyUsedException(String message) {
        super(message);
    }
}
