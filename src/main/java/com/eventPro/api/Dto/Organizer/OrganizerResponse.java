package com.eventPro.api.Dto.Organizer;

import lombok.Builder;
import java.util.UUID;

@Builder
public record OrganizerResponse(
        String name,
        String email,
        String organizationName,
        Boolean isActive
){}