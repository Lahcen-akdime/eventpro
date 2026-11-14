package com.eventPro.api;

import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Service.ReservationExpirationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito cree les @Mock avant chaque test
@MockitoSettings(strictness = Strictness.LENIENT) // pas d'erreur si un stub n'est pas utilise dans un test
class ReservationExpirationServiceTest {

    @Mock
    private ReservationJpaRepository reservationJpaRepository;

    // le service est cree "a la main" dans setUp() car on lui donne un vrai Clock (date fixe)
    private ReservationExpirationService service;

    // date "maintenant" figee : 2026-11-14 10:00
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC);
    private final LocalDateTime now = LocalDateTime.now(clock);

    // la requete envoyee au repository : page 0, taille 100 (BATCH_SIZE)
    private final PageRequest batch = PageRequest.of(0, 100);

    private final UUID id1 = UUID.randomUUID();
    private final UUID id2 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // ordre = ordre des champs final du service
        service = new ReservationExpirationService(reservationJpaRepository, clock);
    }

    // ---------- petit helper (comme OneUser() avant) ----------
    // reservation PENDING_PAYMENT dont isExpiredAt(now) retourne "expiree"
    private Reservation OneReservation(StatutReservation statut, boolean expiree) {
        Reservation r = mock(Reservation.class);
        when(r.getStatutReservation()).thenReturn(statut);
        when(r.isExpiredAt(now)).thenReturn(expiree);
        return r;
    }

    // =====================================================================
    // expireOverdueReservations
    // =====================================================================
    @Test
    void expire_aucuneReservationAExpirer_retourneZero_sansRienVerrouiller() {
        when(reservationJpaRepository.findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch))
                .thenReturn(List.of());

        int result = service.expireOverdueReservations();

        assertEquals(0, result);
        verify(reservationJpaRepository, never()).findByIdForUpdate(any());
        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void expire_deuxReservationsEnRetard_lesExpireToutesLesDeux() {
        // ARRANGE : 1er appel = 2 ids, 2eme appel = liste vide (fin de la boucle)
        Reservation r1 = OneReservation(StatutReservation.PENDING_PAYMENT, true);
        Reservation r2 = OneReservation(StatutReservation.PENDING_PAYMENT, true);
        when(reservationJpaRepository.findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch))
                .thenReturn(List.of(id1, id2), List.of());
        when(reservationJpaRepository.findByIdForUpdate(id1)).thenReturn(Optional.of(r1));
        when(reservationJpaRepository.findByIdForUpdate(id2)).thenReturn(Optional.of(r2));

        // ACT
        int result = service.expireOverdueReservations();

        // ASSERT
        assertEquals(2, result);
        verify(r1).expire();
        verify(r2).expire();
        verify(reservationJpaRepository).save(r1);
        verify(reservationJpaRepository).save(r2);
        verify(reservationJpaRepository, times(2)).findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch);
    }

    @Test
    void expire_utiliseLaDateDeLHorloge_pasLaDateReelle() {
        when(reservationJpaRepository.findIdsToExpire(any(), any(), any())).thenReturn(List.of());

        service.expireOverdueReservations();

        // "now" = la date figee du Clock (2026-11-14 10:00)
        verify(reservationJpaRepository).findIdsToExpire(eq(StatutReservation.PENDING_PAYMENT), eq(now), eq(batch));
    }

    @Test
    void expire_plusieursLots_additionneLesReservationsExpirees() {
        // ARRANGE : lot 1 = id1 expiree, lot 2 = id2 expiree, lot 3 = vide
        Reservation r1 = OneReservation(StatutReservation.PENDING_PAYMENT, true);
        Reservation r2 = OneReservation(StatutReservation.PENDING_PAYMENT, true);
        when(reservationJpaRepository.findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch))
                .thenReturn(List.of(id1), List.of(id2), List.of());
        when(reservationJpaRepository.findByIdForUpdate(id1)).thenReturn(Optional.of(r1));
        when(reservationJpaRepository.findByIdForUpdate(id2)).thenReturn(Optional.of(r2));

        // ACT
        int result = service.expireOverdueReservations();

        // ASSERT
        assertEquals(2, result);
        verify(reservationJpaRepository, times(3)).findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch);
    }

    @Test
    void expire_lotMixte_neCompteQueLesReservationsReellementExpirees() {
        // id1 expiree, id2 pas encore expiree : seule id1 est comptee
        Reservation r1 = OneReservation(StatutReservation.PENDING_PAYMENT, true);
        Reservation r2 = OneReservation(StatutReservation.PENDING_PAYMENT, false);
        when(reservationJpaRepository.findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch))
                .thenReturn(List.of(id1, id2), List.of());
        when(reservationJpaRepository.findByIdForUpdate(id1)).thenReturn(Optional.of(r1));
        when(reservationJpaRepository.findByIdForUpdate(id2)).thenReturn(Optional.of(r2));

        int result = service.expireOverdueReservations();

        assertEquals(1, result);
        verify(r1).expire();
        verify(r2, never()).expire();
        verify(reservationJpaRepository, never()).save(r2);
    }

    @Test
    void expire_reservationDisparueEntreTemps_nonComptee_etLaBoucleS_arrete() {
        // l'id est retourne mais la reservation n'existe plus au moment du verrou
        when(reservationJpaRepository.findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch))
                .thenReturn(List.of(id1));
        when(reservationJpaRepository.findByIdForUpdate(id1)).thenReturn(Optional.empty());

        int result = service.expireOverdueReservations();

        assertEquals(0, result);
        verify(reservationJpaRepository, never()).save(any());
        // aucune reservation expiree dans le lot : on s'arrete, sinon boucle infinie
        verify(reservationJpaRepository, times(1)).findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch);
    }

    @Test
    void expire_reservationDejaConfirmeeEntreTemps_nonExpiree() {
        // un paiement est passe entre la lecture des ids et le verrou : statut CONFIRMED
        Reservation r1 = OneReservation(StatutReservation.CONFIRMED, true);
        when(reservationJpaRepository.findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch))
                .thenReturn(List.of(id1));
        when(reservationJpaRepository.findByIdForUpdate(id1)).thenReturn(Optional.of(r1));

        int result = service.expireOverdueReservations();

        assertEquals(0, result);
        verify(r1, never()).expire();
        verify(reservationJpaRepository, never()).save(any());
        verify(reservationJpaRepository, times(1)).findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch);
    }

    @Test
    void expire_reservationPasEncoreExpiree_nonExpiree_etLaBoucleS_arrete() {
        Reservation r1 = OneReservation(StatutReservation.PENDING_PAYMENT, false);
        when(reservationJpaRepository.findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch))
                .thenReturn(List.of(id1));
        when(reservationJpaRepository.findByIdForUpdate(id1)).thenReturn(Optional.of(r1));

        int result = service.expireOverdueReservations();

        assertEquals(0, result);
        verify(r1, never()).expire();
        verify(reservationJpaRepository, never()).save(any());
        verify(reservationJpaRepository, times(1)).findIdsToExpire(StatutReservation.PENDING_PAYMENT, now, batch);
    }
}