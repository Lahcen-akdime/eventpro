package com.eventPro.api.Utils;

import com.eventPro.api.Entity.Event;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.InvalidVDataException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    public static void validateDates(LocalDateTime startDate, LocalDateTime endDate) {

        if (!startDate.isBefore(endDate)) {
            throw new InvalidVDataException("La date de début doit etre avant la date de fin");
        }

        if (startDate.isAfter(LocalDateTime.now())) {
            throw new InvalidVDataException("La fermeture des ventes ne peut pas dépasser le début de l'événement");
        }

    }

    public static void isOnSaleToday(Event event){
        LocalDateTime now = LocalDateTime.now() ;
        if(event.getOpenDate().isBefore(now)||event.getCloseDate().isBefore(now)){
            throw new InvalidOperationException("L'evenement est pas on vente maintenant ! ");
        }
    }
}
