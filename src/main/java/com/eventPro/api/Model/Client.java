package com.eventPro.api.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class Client extends User{

    private String phone ;

    @OneToMany
    private List<Reservation> reservations ;

    public Client(String name, String email, String phone, List<Reservation> reservations) {
        super(name, email);
        this.phone = phone;
        this.reservations = reservations;
    }

    public Client(String phone, List<Reservation> reservations) {
        this.phone = phone;
        this.reservations = reservations;
    }

    public Client() {

    }
}
