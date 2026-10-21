package com.eventPro.api.Model;

import com.eventPro.api.Model.Enum.StatutEvenement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "events")
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @NotBlank
    @NotNull
    private String title ;

    @NotBlank
    @NotNull
    private String description ;

    private String place ;

    private LocalDateTime start ;

    private LocalDateTime end ;

    private LocalDateTime openDate ;

    private LocalDateTime closeDate ;

    @Enumerated(EnumType.STRING)
    private StatutEvenement state ;

    @JoinColumn(name = "organizer_id",referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user ;

}
