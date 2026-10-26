package com.eventPro.api.Entity;

import jakarta.persistence.Entity;
import lombok.experimental.SuperBuilder;


@Entity
@SuperBuilder
public class Organizer extends User {

    private String organizationName ;

    public Organizer() {

    }
}