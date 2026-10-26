package com.eventPro.api.Utils;

import com.eventPro.api.Exception.InvalidVDataException;

import java.time.LocalDateTime;

public class DateUtils {

    public static void ValidateStartAndEndDates(LocalDateTime start , LocalDateTime end){
        if(end.isBefore(start)){
            throw new InvalidVDataException("Les date de debut est avant la date de fin") ;
        }
    }
}
