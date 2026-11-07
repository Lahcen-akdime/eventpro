package com.eventPro.api.Service;

import com.eventPro.api.Dto.Reservation.ReservationCreateRequest;
import com.eventPro.api.Dto.Reservation.ReservationResponse;
import com.eventPro.api.Entity.*;
import com.eventPro.api.Entity.Enum.StatutPaiement;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Exception.InsufficientStockException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Mapper.ReservationMapper;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Repository.ReservationLineJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Utils.ReferenceGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationJpaRepository reservationJpaRepository ;
    private final TicketCategoryRepository ticketCategoryRepository ;
    private final PromotionService promotionService ;
    private final ClientService clientService ;
    private final UserService userService ;
    private final EventService eventService ;
    private final ReservationMapper reservationMapper ;
    private final ReservationLineJpaRepository reservationLineJpaRepository;
    private static final Duration HOLD_DURATION = Duration.ofMinutes(10);
    private static final List<StatutReservation> ACTIVE_STATUSES =
            List.of(StatutReservation.PENDING_PAYMENT, StatutReservation.CONFIRMED);
    private final Clock clock;

    @Transactional
    public ReservationResponse create( ReservationCreateRequest request , HttpServletRequest servletRequest){
        // User get and check
        String email = UserService.getAuthenticatedEmail(servletRequest) ;
        Client client = clientService.findByEmail(email) ;
        userService.checkIsActive();
        // Current time
        LocalDateTime now = LocalDateTime.now(clock);
        // Event get and check
        Event event = eventService.findEvent(request.eventId());
        if (!event.isOnSale(now)) {
            throw new InvalidOperationException("La vente n'est pas ouverte pour ce event");
        }
        List<UUID> ids = request.lines().stream().map(line -> line.categoryId()).toList();
        // lock categorys
        List<TicketCategory> locked = ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(ids);
        Map<UUID, TicketCategory> categories = locked.stream()
                .collect(Collectors.toMap(TicketCategory::getId, Function.identity()));
        // verifier quota par client
        for (var line : request.lines()) {
            TicketCategory category = categories.get(line.categoryId());
            long quantity = line.quantity();
            long alreadyTaken = reservationLineJpaRepository
                    .sumActiveQuantity(client.getId(), category.getId(),ACTIVE_STATUSES);
            if(quantity > category.getAvailablePlaces()){
                throw new InsufficientStockException("quantite est plus que la capacite du event");
            }
            if (alreadyTaken + quantity > category.getMaxPerClient()) {
                throw new InvalidOperationException("le client veux plus que possible ");
            }
        }
        // creer réservation
        Reservation reservation = Reservation.builder()
                .reference(ReferenceGenerator.getNewReference())
                .statutReservation(StatutReservation.PENDING_PAYMENT)
                .client(client)
                .event(event)
                .dateCreation(now)
                .dateExpiration(now.plus(HOLD_DURATION))
                .build();
        // add lines to reservation
        for (var line : request.lines()) {
            TicketCategory category = categories.get(line.categoryId());
            int quantity = line.quantity();
            category.reserve(quantity);
            BigDecimal unitPrice = category.getUnitPrice();
            reservation.addLine(LigneReservation.builder()
                    .ticketCategory(category)
                    .quantite(quantity)
                    .unitPrice(unitPrice)
                    .subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP))
                    .build());
        }
        //  code promo
        if (request.promoCode() != null && !request.promoCode().isBlank()) {

            Promotion promotion = promotionService.findByCode(request.promoCode().trim()) ;
                    if(!promotion.isValidAt(now) ){
                        throw new InvalidOperationException("Code promo invalide a "+now) ;
                    } else if (!promotion.isApplicableTo(event)) {
                        throw new InvalidOperationException("Code promo invalide a ce event "+event.getTitle()) ;
                    }
            promotion.addUtilisation(1L);
            reservation.applyPromotion(promotion);
        }
        reservation.calculateTotal();
        // Pending Paiment 
        Paiment paiement = Paiment.builder()
                .reference(ReferenceGenerator.getNewReference())
                .amount(reservation.getDiscountAmount())
                .paidAt(now)
                .statut(StatutPaiement.PENDING)
                .reservation(reservation)
                .build();
        // save
        Reservation saved = reservationJpaRepository.save(reservation);
        return reservationMapper.toResponse(saved);
    }

    @Transactional
    public Page<ReservationResponse> getMyReservations(Pageable pageable, HttpServletRequest servletRequest){
        String email = UserService.getAuthenticatedEmail(servletRequest) ;
        Client client = clientService.findByEmail(email) ;
        return reservationJpaRepository.getAllByClient(client,pageable).map(reservationMapper::toResponse) ;
    }

    @Transactional
    public ReservationResponse getById(UUID id){
        Optional<Reservation> reservation = reservationJpaRepository.findById(id) ;
        if(!reservation.isPresent()){
            throw new InvalidVDataException("reservation not found") ;
        }
        return reservationMapper.toResponse(reservation.get()) ;
    }

    @Transactional
    public Reservation findById(UUID id){
        Optional<Reservation> reservation = reservationJpaRepository.findById(id) ;
        if(!reservation.isPresent()){
            throw new InvalidVDataException("reservation not found") ;
        }
        return reservation.get() ;
    }

    @Transactional
    public void cancel(UUID reservationId){
        Reservation reservation = findById(reservationId) ;
        reservation.cancel() ;
        reservationJpaRepository.save(reservation) ;
    }

    @Transactional
    public Page<ReservationResponse> getList(@PageableDefault(size = 3) Pageable pageable){
        return reservationJpaRepository.findAll(pageable).map(reservationMapper::toResponse) ;
    }

    public Optional<Reservation> findByIdForUpdate(UUID reservationId){
        Reservation reservation = reservationJpaRepository.findByIdForUpdate(reservationId) ;
        return Optional.of(reservation) ;
    }

}
