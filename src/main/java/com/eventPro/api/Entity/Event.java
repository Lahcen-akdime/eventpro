package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.StatutEvenement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "events")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @NotNull
    private String title ;

    @NotNull
    private String description ;

    private String place ;

    private LocalDateTime start_date ;

    private LocalDateTime end_date ;

    private LocalDateTime openDate ;

    private LocalDateTime closeDate ;

    @Enumerated(EnumType.STRING)
    private StatutEvenement state ;

    @JoinColumn(name = "organizer_id",referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Organizer organizer ;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "event")
    private List<TicketCategory> tickets ;


    public void publier(){
        state = StatutEvenement.PUBLISHED ;
    }

    public void annuler(){
        state = StatutEvenement.CANCELLED ;
    }


}
