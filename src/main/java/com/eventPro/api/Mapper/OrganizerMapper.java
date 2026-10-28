package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.Organizer.OrganizerRegisterRequest;
import com.eventPro.api.Dto.Organizer.OrganizerResponse;
import com.eventPro.api.Entity.Organizer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizerMapper {

    Organizer toEntity(OrganizerRegisterRequest request);

    OrganizerResponse toDtoResponse(Organizer organizer);
}