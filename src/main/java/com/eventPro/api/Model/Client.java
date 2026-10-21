package com.eventPro.api.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "clients")
@NoArgsConstructor
public class Client extends User{

    private String phone ;

    @OneToMany
    private List<Reservation> reservations ;

    public Client(String name, String email, String phone, List<Reservation> reservations) {
        super(name, email);
        this.phone = phone;
        this.reservations = reservations ;
    }

    public Client(String phone, List<Reservation> reservations ) {
        this.phone = phone;
        this.reservations = reservations;
    }

}
