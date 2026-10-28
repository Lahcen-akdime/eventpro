
package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.Promotion.PromotionCreateRequest;
import com.eventPro.api.Dto.Promotion.PromotionResponse;
import com.eventPro.api.Entity.Promotion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Builder;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "event", ignore = true)
    Promotion toEntity(PromotionCreateRequest request);

    PromotionResponse toResponse(Promotion promotion) ;
}
