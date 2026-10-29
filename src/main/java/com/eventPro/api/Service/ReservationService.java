package com.eventPro.api.Service;

import com.eventPro.api.Dto.Reservation.ReservationCreateRequest;
import com.eventPro.api.Dto.Reservation.ReservationResponse;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Mapper.ReservationMapper;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Utils.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationJpaRepository reservationJpaRepository ;
    private final TicketCategoryRepository ticketCategoryRepository ;
    private final ClientService clientService ;
    private final UserService userService ;
    private final EventService eventService ;
    private final ReservationMapper reservationMapper ;

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
        List<TicketCategory> list = ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(ids) ;
        if(!list.stream().findFirst().isPresent() || list.stream().count() != ids.stream().count()){
            throw new InvalidOperationException("les categories sont incorrect") ;
        }
        //



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
