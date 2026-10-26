package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.ClientRegisterRequest;
import com.eventPro.api.Dto.ClientResponse;
import com.eventPro.api.Entity.Client;
import org.mapstruct.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    ClientRegisterRequest toDto(Client client) ;

    ClientResponse toDtoResponse(Client client) ;

    @Mappings({
            @Mapping(target = "password",ignore = true),
            @Mapping(target = "reservations",ignore = true),
            @Mapping(target = "id",ignore = true),
            @Mapping(target = "role",ignore = true),
    })
    Client toEntity(ClientRegisterRequest request) ;

}
