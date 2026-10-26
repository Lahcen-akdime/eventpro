package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.CreateEventDto;
import com.eventPro.api.Dto.EventResponse;
import com.eventPro.api.Entity.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    CreateEventDto toDto(Event event) ;
    EventResponse toDtoResponse(Event event) ;
    Event toEntity(CreateEventDto createEvent) ;
    }
