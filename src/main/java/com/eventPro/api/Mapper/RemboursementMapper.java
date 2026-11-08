
package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.Remboursement.RemboursementResponse;
import com.eventPro.api.Entity.Remboursement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RemboursementMapper {

    RemboursementResponse toResponse(Remboursement remboursement);
}
