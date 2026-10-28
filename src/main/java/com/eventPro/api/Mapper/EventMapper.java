package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.Event.EventCreateRequest;
import com.eventPro.api.Dto.Event.EventResponse;
import com.eventPro.api.Dto.Event.EventUpdateRequest;
import com.eventPro.api.Entity.Event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    Event toEntity(EventCreateRequest request);

    @Mapping(target = "organizerId", source = "organizer.id")
    @Mapping(target = "organizerName", source = "organizer.name")
    @Mapping(target = "organizationName", source = "organizer.organizationName")
    EventResponse toResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    void updateEntity(EventUpdateRequest request, @MappingTarget Event event);
}