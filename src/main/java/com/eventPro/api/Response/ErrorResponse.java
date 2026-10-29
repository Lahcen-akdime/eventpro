package com.eventPro.api.Response;

import lombok.Builder;

import java.util.Date;

@Builder
public record ErrorResponse (

     Date timestamp ,

     int statusCode ,

     String message ,

    // To understand
     String path
) {}
