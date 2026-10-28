package com.eventPro.api.Advice;


import com.eventPro.api.Error.ErrorResponse;
import com.eventPro.api.Exception.*;
import jakarta.transaction.SystemException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request){

        return buildTheError(ex,HttpStatus.NOT_FOUND.value(),request) ;

    }

    @ExceptionHandler(InvalidVDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidVDataException(InvalidVDataException ex, WebRequest request){

        return buildTheError(ex,HttpStatus.BAD_REQUEST.value(),request) ;

    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEmailAlreadyExistException(EmailAlreadyExistException ex, WebRequest request){

        return buildTheError(ex,HttpStatus.BAD_REQUEST.value(),request) ;

    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInsufficientStockException(InsufficientStockException ex, WebRequest request){

        return buildTheError(ex,HttpStatus.CONFLICT.value(),request) ;

    }

    @ExceptionHandler(TicketAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTicketAlreadyUsedException(TicketAlreadyUsedException ex, WebRequest request){

        return buildTheError(ex,HttpStatus.CONFLICT.value(),request) ;


    }

    @ExceptionHandler(IdempotencyConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIdempotencyConflictException(IdempotencyConflictException ex, WebRequest request){

        return buildTheError(ex,HttpStatus.CONFLICT.value(),request) ;

    }

    @ExceptionHandler(ReservationExpiredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleReservationExpiredException(ReservationExpiredException ex, WebRequest request){

        return buildTheError(ex,HttpStatus.CONFLICT.value(),request) ;

    }

    @ExceptionHandler(EventAlreadyPublishedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEventAlreadyPublishedException(EventAlreadyPublishedException ex, WebRequest request){

        return buildTheError(ex,HttpStatus.CONFLICT.value(),request) ;

    }

    @ExceptionHandler(DataIntegrityViolationException .class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse   handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request){
        return buildTheError(ex,HttpStatus.CONFLICT.value(),request) ;
    }

    @ExceptionHandler(EmailNotFoundException .class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse   handleEmailNotFoundException(EmailNotFoundException ex, WebRequest request){
        return buildTheError(ex,HttpStatus.CONFLICT.value(),request) ;
    }

    @ExceptionHandler(InvalidCredantialsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInvalidCredantialsException(InvalidCredantialsException ex,WebRequest request){
        return buildTheError(ex,HttpStatus.CONFLICT.value(),request) ;
    }

    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEventNotFoundException(EventNotFoundException ex,WebRequest request){
        return buildTheError(ex,HttpStatus.CONFLICT.value(),request) ;
    }

    @ExceptionHandler(InvalidOperationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInvalidOperationException(InvalidOperationException ex,WebRequest request){
        return buildTheError(ex,HttpStatus.CONFLICT.value(),request) ;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex, WebRequest request){

        return buildTheError(ex,HttpStatus.FORBIDDEN.value(),request) ;

    }

    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleSystemException(SystemException ex,WebRequest request){
         return buildTheError(ex,HttpStatus.INTERNAL_SERVER_ERROR.value(),request) ;
    }

    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleResponseStatusException(ResponseStatusException ex,WebRequest request){
        return buildTheError(ex,ex.getStatusCode().value(),request) ;
    }

    @ExceptionHandler(UserNotLoggedInException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUserNotLoggedInException(UserNotLoggedInException ex,WebRequest request){
        return buildTheError(ex,HttpStatus.UNAUTHORIZED.value(),request) ;
    }



    public ErrorResponse buildTheError(Exception ex ,int status,WebRequest request){
        return ErrorResponse.builder()
                .timestamp(new Date())
                .statusCode(status)
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
    }

}
