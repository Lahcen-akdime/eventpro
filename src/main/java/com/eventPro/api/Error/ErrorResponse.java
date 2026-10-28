package com.eventPro.api.Error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.web.error.Error;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Builder
public record ErrorResponse (

     Date timestamp ,

     int statusCode ,

     String message ,

    // To understand
     String path
) {}
