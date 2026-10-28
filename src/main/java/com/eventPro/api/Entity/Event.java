package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.StatutEvenement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotBlank
    @Size(max = 150)
    private String title ;

    @NotNull
    @NotBlank
    private String description ;

    @NotBlank
    @Column(nullable = false)
    @Size(max = 255)
    private String place ;

    @Column(nullable = false)
    private LocalDateTime startDate ;

    @Column(nullable = false)
    private LocalDateTime endDate ;

    @Column(nullable = false)
    private LocalDateTime openDate ;

    @Column(nullable = false)
    private LocalDateTime closeDate ;

    @Enumerated(EnumType.STRING)
    private StatutEvenement state ;

    @Column(nullable = false)
    private int capacite ;

    @JoinColumn(name = "organizer_id",referencedColumnName = "id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
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
