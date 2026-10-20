package com.eventPro.api.Model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.UUID;

public class LigneReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    private Long quantite ;

    private BigDecimal unitPrice ;

    private BigDecimal subtotal ;

}
