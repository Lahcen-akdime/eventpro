package com.eventPro.api.Controller;

import com.eventPro.api.Error.ErrorResponse;
import com.eventPro.api.Exception.*;
import jakarta.transaction.SystemException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request){

         return ErrorResponse.builder()
                 .timestamp(new Date())
                 .statusCode(HttpStatus.NOT_FOUND.value())
                 //.message("Ressource not found !!")
                 .message(ex.getMessage())
                 .description(request.getDescription(false))
                 .build();

    }

    @ExceptionHandler(InvalidVDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidVDataException(InvalidVDataException ex, WebRequest request){

        return ErrorResponse.builder()
                .timestamp(new Date())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .fieldErrors(ex.getFieldsError())
                //.message("data not valid !!")
                .message(ex.getMessage())
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInsufficientStockException(InsufficientStockException ex, WebRequest request){

        return ErrorResponse.builder()
                .timestamp(new Date())
                .statusCode(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                //.message("The stock is not enouph !!")
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(TicketAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTicketAlreadyUsedException(TicketAlreadyUsedException ex, WebRequest request){

        return ErrorResponse.builder()
                .timestamp(new Date())
                .statusCode(HttpStatus.CONFLICT.value())
                //.message("The Ticket is Already Used !!")
                .message(ex.getMessage())
                .description(request.getDescription(false))
                .build();

    }

    @ExceptionHandler(IdempotencyConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIdempotencyConflictException(IdempotencyConflictException ex, WebRequest request){

        return ErrorResponse.builder()
                .timestamp(new Date())
                .statusCode(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                //.message("The Idempotency face a Conflict !!")
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(ReservationExpiredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleReservationExpiredException(ReservationExpiredException ex, WebRequest request){

        return ErrorResponse.builder()
                .timestamp(new Date())
                .statusCode(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                //.message("The Reservation is Expired !!")
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex, WebRequest request){

        return ErrorResponse.builder()
                .timestamp(new Date())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message(ex.getMessage())
                //.message("You dont heve permission to access this !!")
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleSystemException(SystemException ex, WebRequest request){

        return ErrorResponse.builder()
                .timestamp(new Date())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .description(request.getDescription(false))
                .build();
    }

}
