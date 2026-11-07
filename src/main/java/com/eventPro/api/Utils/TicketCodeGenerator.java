package com.eventPro.api.Utils;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component 
public class TicketCodeGenerator {

    public static String getNewTicketCode(){
        return "TICK-"+ UUID.randomUUID().toString().substring(0,15) ;
    }
    
}
