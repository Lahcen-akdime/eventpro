package com.eventPro.api.Service;

import com.eventPro.api.Dto.Event.EventCreateRequest;
import com.eventPro.api.Dto.Event.EventResponse;
import com.eventPro.api.Dto.Event.EventUpdateRequest;
import com.eventPro.api.Entity.Enum.StatutEvenement;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Organizer;
import com.eventPro.api.Exception.EmailNotFoundException;
import com.eventPro.api.Exception.EventNotFoundException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Mapper.EventMapper;
import com.eventPro.api.Repository.EventJpaRepository;
import com.eventPro.api.Repository.OrganizerJpaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventJpaRepository eventJpaRepository;
    private final OrganizerJpaRepository organizerJpaRepository;
    private final EventMapper eventMapper;

    @Transactional
    public EventResponse create(EventCreateRequest request, String email) {
        validateDates(request.startDate(), request.endDate(), request.openDate(), request.closeDate());
        Organizer organizer = organizerJpaRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new EmailNotFoundException("organizer introuvable"));
        Event event = eventMapper.toEntity(request);
        event.setOrganizer(organizer);
        event.setState(StatutEvenement.DRAFT);
        return eventMapper.toResponse(eventJpaRepository.save(event));
    }

    @Transactional(readOnly = true)
    public EventResponse getById(UUID id) {
        Event event = findEvent(id);
        return eventMapper.toResponse(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getAll(Pageable pageable) {
        return eventJpaRepository.findAll(pageable)
                .map(eventMapper::toResponse);
    }

    @Transactional
    public EventResponse update(UUID id, EventUpdateRequest request) {
        Event event = findEvent(id);
        if (event.getState() != StatutEvenement.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un draft peut etre publie");
        }
        validateDates(request.startDate(), request.endDate(), request.openDate(), request.closeDate());
        eventMapper.updateEntity(request, event);
        return eventMapper.toResponse(
                eventJpaRepository.save(event)
        );
    }

    @Transactional
    public EventResponse publish(UUID id) {
        Event event = findEvent(id);
        if (event.getState() != StatutEvenement.DRAFT) {
            throw new InvalidVDataException("event is not draft");
        }
        validateDates(event.getStartDate(), event.getEndDate(), event.getOpenDate(), event.getCloseDate());
        event.publier();
        return eventMapper.toResponse(eventJpaRepository.save(event));
    }

    @Transactional
    public EventResponse cancel(UUID id) {
        Event event = findEvent(id);
        if (event.getState().equals(StatutEvenement.CANCELLED)) {
            throw new InvalidVDataException("event already canceled");
        }
        validateDates(event.getStartDate(), event.getEndDate(), event.getOpenDate(), event.getCloseDate());
        event.annuler();
        return eventMapper.toResponse(eventJpaRepository.save(event));
    }



    public Event findEvent(UUID id) {
        return eventJpaRepository.findById(id).orElseThrow(() -> new EventNotFoundException("Événement introuvable"));
    }

    public void validateDates(LocalDateTime startDate, LocalDateTime endDate, LocalDateTime openDate, LocalDateTime closeDate) {
        if (!startDate.isBefore(endDate)) {
            throw new InvalidVDataException("La date de début doit etre avant la date de fin");
        }
        if (!openDate.isBefore(closeDate)) {
            throw new InvalidVDataException("L'ouverture des ventes doit etre avant leur fermeture");
        }
        if (closeDate.isAfter(startDate)) {
            throw new InvalidVDataException("La fermeture des ventes ne peut pas dépasser le début de l'événement");
        }
    }
}