
package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.Reservation.*;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "event.title", target = "eventTitle")
    @Mapping(source = "dateCreation", target = "createdAt")
    @Mapping(source = "dateExpiration", target = "expiresAt")
    @Mapping(source = "statutReservation", target = "status")
    @Mapping(source = "promotion.code", target = "promoCode")
    ReservationResponse toResponse(Reservation reservation);

    @Mapping(source = "ticketCategory.id", target = "categoryId")
    @Mapping(source = "ticketCategory.name", target = "categoryName")
    @Mapping(source = "quantite", target = "quantity")
    @Mapping(source = "subtotal", target = "subTotal")
    LigneReservationResponse toLineResponse(LigneReservation ligne);

    default Instant map(LocalDateTime value) {
        return value == null ? null : value.atZone(ZoneId.systemDefault()).toInstant();
    }

}
