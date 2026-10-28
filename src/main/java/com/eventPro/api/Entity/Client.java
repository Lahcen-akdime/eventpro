package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Reservation.Reservation;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "clients")
@NoArgsConstructor
@SuperBuilder
public class Client extends User {

    private String phone ;

    @OneToMany(mappedBy = "client")
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

}
