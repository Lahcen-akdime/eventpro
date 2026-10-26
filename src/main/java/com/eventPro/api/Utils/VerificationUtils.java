package com.eventPro.api.Utils;

import com.eventPro.api.Exception.EventAlreadyPublishedException;
import com.eventPro.api.Entity.Enum.StatutEvenement;
import com.eventPro.api.Entity.Event;

public class VerificationUtils {

    public static Boolean verifierStock(Long stock , Long quantite){
        if(stock >= quantite){
            return true ;
        } else return false ;
    }

    public static Boolean verifierEventNotPublished(Event event){
        if (event.getState().equals(StatutEvenement.DRAFT)){
            return true ;
        }
        else {
            throw new EventAlreadyPublishedException("L'evenement est deja publier ! ");
        }
    }

}
