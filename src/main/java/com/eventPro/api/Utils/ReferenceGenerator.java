package com.eventPro.api.Utils;

import org.springframework.stereotype.Component;

import javax.xml.crypto.AlgorithmMethod;
import java.security.SecureRandom;
import java.util.UUID;

@Component
public class ReferenceGenerator {

    public static String getNewReference(){
        return "Ref-"+ UUID.randomUUID().toString().substring(0,5) ;
    }

}
