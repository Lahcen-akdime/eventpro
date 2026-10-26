package com.eventPro.api.Service;

import com.eventPro.api.Dto.CreateEventDto;
import com.eventPro.api.Dto.EventResponse;
import com.eventPro.api.Entity.Enum.StatutEvenement;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository ;

    public EventResponse save(CreateEventDto eventDto){
//        Event event = Event.builder()
//                            .title(title)
//                            .description(description)
//                            .place(place)
//                            .start_date(start_date)
//                            .end_date(end_date)
//                            .openDate(openDate)
//                            .closeDate(closeDate)
//                            .build();
        //return eventRepository.save(event) ;
        return null ;
    }

    public List<Event> searchByStatus(StatutEvenement state){
        return eventRepository.searchByState(state) ;
    }

}
