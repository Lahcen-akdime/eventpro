package com.eventPro.api.Dto;

import com.eventPro.api.Entity.Enum.StatutEvenement;
import com.eventPro.api.Entity.TicketCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public class EventResponse {

    @NotNull
    @NotBlank
    private String title ;

    private String description ;

    @NotNull
    @NotBlank
    private String place ;

    @NotNull
    private LocalDateTime start_date ;

    @NotNull
    private LocalDateTime end_date ;

    @NotNull
    private LocalDateTime openDate ;

    @NotNull
    private LocalDateTime closeDate ;

    private StatutEvenement statutEvenement ;

    @NotNull
    @NotEmpty
    private List<TicketCategory> categories ;

    private Long disponiblePlaces ;
}
