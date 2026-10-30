package com.eventPro.api.Service;

import com.eventPro.api.Dto.Reservation.ReservationCreateRequest;
import com.eventPro.api.Dto.Reservation.ReservationResponse;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Paiment;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Exception.InsufficientStockException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Mapper.ReservationMapper;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Utils.DateUtils;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationJpaRepository reservationJpaRepository ;
    private final TicketCategoryRepository ticketCategoryRepository ;
    private final ClientService clientService ;
    private final UserService userService ;
    private final EventService eventService ;
    private final ReservationMapper reservationMapper ;
    private final TicketCategoryService ticketCategoryService;

    @Transactional
    public ReservationResponse create( ReservationCreateRequest request , HttpServletRequest servletRequest){
        // User get and check
        String email = UserService.getAuthenticatedEmail(servletRequest) ;
        Client client = clientService.findByEmail(email) ;
        userService.checkIsActive();
        // Event get and check
        Event event = eventService.findEvent(request.eventId()) ;
        DateUtils.isOnSaleToday(event);
        // Category get , lock and check
        List<UUID> ids = request.lines().stream().map(line->line.categoryId()).toList() ;
        List<TicketCategory> categories = ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(ids) ;
        if(!categories.stream().findFirst().isPresent() || categories.stream().count() != ids.stream().count()){
            throw new InvalidOperationException("les categories sont incorrect") ;
        }
        // Prepare categories
        Map<UUID,TicketCategory> mapcategories = new HashMap<>() ;
        categories.stream().forEach(c->{
            mapcategories.put(c.getId(),c);
        });
        //Prepare lines
        List<LigneReservation> ligneReservations = new ArrayList<>() ;

        // Verify quota client
        request.lines().stream().forEach(line->{
            TicketCategory category = mapcategories.get(line.categoryId()) ;
            // Check capacite
            if( category.getQuotaMaxParClient() < line.quantity()){
                throw new InvalidOperationException("Tu peux pas reserver plus que "+category.getQuotaMaxParClient()) ;
            } else if (line.quantity() > category.getAvailblePlaces()) {
                throw new InsufficientStockException("Le stock est pas sufficient pour cette demande") ;
            }
            // Reserve
            category.reserve(line.quantity()) ;
        });
        //
        Reservation reservation = Reservation.builder().statutReservation(StatutReservation.PENDING_PAYMENT)
                                            .event(event).client(client).dateExpiration(LocalDateTime.now()
                                            .plusMinutes(10)).build();
        request.lines().forEach(line->{
            //Add ligne
            LigneReservation ligneReservation = LigneReservation.builder().subtotal()
            //private Long quantite ;
            //private BigDecimal unitPrice ;
            //private BigDecimal subtotal ;
            //private Reservation reservation ;
            //ligneReservations.add()
        });


        // Payement
        //Paiment paiment = Paiment.builder().key().montant().paymentDate().paymentStates().reservation().build()







        //Reservation reservation = Reservation.builder() ;
        return null ;
    }

    public Page<ReservationResponse> getMyReservations(Pageable pageable, HttpServletRequest servletRequest){
        String email = UserService.getAuthenticatedEmail(servletRequest) ;
        Client client = clientService.findByEmail(email) ;
        return reservationJpaRepository.getAllByClient(client,pageable).map(reservationMapper::toResponse) ;
    }

    public ReservationResponse getById(UUID id){
        Optional<Reservation> reservation = reservationJpaRepository.findById(id) ;
        if(!reservation.isPresent()){
            throw new InvalidVDataException("reservation not found") ;
        }
        return reservationMapper.toResponse(reservation.get()) ;
    }

    public void cancel(UUID reservationId){

    }

    public Page<ReservationResponse> getList(@PageableDefault(size = 3) Pageable pageable){
        return reservationJpaRepository.findAll(pageable).map(reservationMapper::toResponse) ;
    }


}
