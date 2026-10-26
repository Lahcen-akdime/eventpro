package com.eventPro.api.Controller;

import com.eventPro.api.Dto.CreateEventDto;
import com.eventPro.api.Dto.EventResponse;
import com.eventPro.api.Service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/api/v1/event")
public class EventController {

    private final EventService eventService ;

    @PostMapping
    public EventResponse save(@RequestBody CreateEventDto eventDto){
        return eventService.save(eventDto) ;
    }
}
