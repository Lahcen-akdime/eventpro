package com.eventPro.api.Service;

import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationExpirationService {

    private static final int BATCH_SIZE = 100;

    private final ReservationJpaRepository reservationJpaRepository;
    private final Clock clock;

    @Transactional
    public int expireOverdueReservations() {
        LocalDateTime now = LocalDateTime.now(clock);
        int expiredCount = 0;

        while (true) {
            List<UUID> reservationIds = reservationJpaRepository.findIdsToExpire(
                    StatutReservation.PENDING_PAYMENT, now, PageRequest.of(0, BATCH_SIZE));
            if (reservationIds.isEmpty()) {
                return expiredCount;
            }

            int expiredInBatch = 0;
            for (UUID reservationId : reservationIds) {
                if (expireIfOverdue(reservationId, now)) {
                    expiredInBatch++;
                }
            }
            expiredCount += expiredInBatch;

            if (expiredInBatch == 0) {
                return expiredCount;
            }
        }
    }

    private boolean expireIfOverdue(UUID reservationId, LocalDateTime now) {
        Reservation reservation = reservationJpaRepository.findByIdForUpdate(reservationId)
                .orElse(null);
        if (reservation == null
                || reservation.getStatutReservation() != StatutReservation.PENDING_PAYMENT
                || !reservation.isExpiredAt(now)) {
            return false;
        }

        reservation.expire();
        reservationJpaRepository.save(reservation);
        return true;
    }
}
