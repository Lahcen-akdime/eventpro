package com.eventPro.api.Error;

import lombok.Builder;
import org.springframework.boot.web.error.Error;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.util.List;

@Builder
public class ErrorResponse {

    private Timestamp timestamp ;

    private HttpStatus status ;

    private String error ;

    private String message ;

    private String path ;

    private List<Error> fieldErrors ;
}
