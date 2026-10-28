
package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.Reservation.*;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "statutReservation", target = "status")
    @Mapping(source = "promotion.code", target = "promoCode")
    ReservationResponse toResponse(Reservation reservation);

    @Mapping(source = "quantite", target = "quantity")
    @Mapping(source = "subtotal", target = "subTotal")
    LigneReservationResponse toLineResponse(LigneReservation ligne);

}
