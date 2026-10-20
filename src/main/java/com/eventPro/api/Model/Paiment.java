package com.eventPro.api.Model;

import com.eventPro.api.Model.Enum.StatutPaiement;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Paiment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    private String reference ;

    private BigDecimal montant ;

    private LocalDateTime paymentDate ;

    private StatutPaiement paymentStates ;

}
