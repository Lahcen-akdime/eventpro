package com.eventPro.api.Controller;

import com.eventPro.api.Service.OrganizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizers")
@RequiredArgsConstructor
public class OrganizersController {

    private final OrganizerService service ;

    //@PostMapping
    //public User save(@Valid @RequestBody CreateOrganizerDto dto){
    //    return service.save(CreateOrganizerDto dto) ;
    //}

}
