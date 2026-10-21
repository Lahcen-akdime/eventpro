package com.eventPro.api.Exception;

import lombok.Getter;

import java.util.List;

@Getter
public class InvalidVDataException extends RuntimeException {

    public List<String> fieldsError ;

    public InvalidVDataException(String message) {
        super(message);
    }

    public void setErrorField(String field){
        fieldsError.add(field) ;
    }

}
