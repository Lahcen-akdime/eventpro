
package com.eventPro.api.Controller.Organizer;

import com.eventPro.api.Dto.Promotion.PromotionCreateRequest;
import com.eventPro.api.Dto.Promotion.PromotionResponse;
import com.eventPro.api.Dto.Promotion.PromotionUpdateRequest;
import com.eventPro.api.Mapper.PromotionMapper;
import com.eventPro.api.Service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizers/promotions")
@RequiredArgsConstructor
@Tag(name = "Organizer promotions")
public class OrganizerPromotionController {

    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper ;

    @PostMapping
    @Operation(summary = "create promotion")
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponse create( @Valid @RequestBody PromotionCreateRequest request) {
        PromotionResponse response = promotionService.create(request);
        return response ;
    }

    @GetMapping
    @Operation(summary = "Lister les promotions de l'organisateur")
    public Page<PromotionResponse> list(@PageableDefault(size = 20) Pageable pageable) {

        return promotionService.getList(pageable) ;
    }

    @GetMapping("/{promotionId}")
    @Operation(summary = "Consulter une promotion")
    public PromotionResponse getById(@PathVariable UUID promotionId) {
        return promotionMapper.toResponse(promotionService.findById(promotionId)) ;
    }

    @PutMapping("/{promotionId}")
    @Operation(summary = "Modifier une promotion")
    public PromotionResponse update(@PathVariable UUID promotionId,
                                    @Valid @RequestBody PromotionUpdateRequest request) {
        return promotionService.update(promotionId, request) ;
    }

}
