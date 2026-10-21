package com.eventPro.api.Error;

import lombok.Builder;
import lombok.Setter;
import org.springframework.boot.web.error.Error;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Builder
public class ErrorResponse {

    private Date timestamp ;

    private int statusCode ;

    private String message ;

    private String description ;

    // To understand
    private String path ;

    // To use it later
     private List<String> fieldErrors ;

}
