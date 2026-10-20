package com.eventPro.api.Model;

import com.eventPro.api.Model.Enum.StatutReservation;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String reference ;

    private LocalDateTime dateCreation = LocalDateTime.now() ;

    private LocalDateTime dateExpiration  ;

    private LocalDateTime startEvent ;

    private LocalDateTime endEvent ;

    private StatutReservation statutReservation ;
    private String title ;
    private String description ;
    private String place ;



}
