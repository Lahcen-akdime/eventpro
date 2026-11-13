package com.eventPro.api.Service;

import com.eventPro.api.Dto.Promotion.PromotionCreateRequest;
import com.eventPro.api.Dto.Promotion.PromotionResponse;
import com.eventPro.api.Dto.Promotion.PromotionUpdateRequest;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Promotion;
import com.eventPro.api.Exception.EmailNotFoundException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.ResourceNotFoundException;
import com.eventPro.api.Mapper.PromotionMapper;
import com.eventPro.api.Repository.PromotionJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionJpaRepository promotionJpaRepository ;
    private final PromotionMapper promotionMapper ;
    private final EventService eventService ;

    @Transactional
    public PromotionResponse create(PromotionCreateRequest request){
        Event event = eventService.findEvent(request.event_id()) ;
        Promotion promotion = Promotion.builder().code(request.code())
                                                .typeReduction(request.typeReduction())
                                                .valeurReduction(request.valeurReduction())
                                                .startDate(request.startDate())
                                                .endDate(request.endDate())
                                                .limiteUtilisation(request.limiteUtilisation())
                                                .event(event)
                                                .build();
        return promotionMapper.toResponse(promotionJpaRepository.save(promotion)) ;
    }

    public Page<PromotionResponse> getList(Pageable pageable){
        return promotionJpaRepository.findAll(pageable).map(promotionMapper::toResponse) ;
    }

    public Promotion findById(UUID id){
        Optional<Promotion> promotion = promotionJpaRepository.findById(id) ;
        if(!promotion.isPresent()){
            throw new ResourceNotFoundException("The promotion is not found");
        }
        return promotion.get() ;
    }

    public Promotion findByCode(String code){
        Optional<Promotion> promotion = promotionJpaRepository.findByCode(code) ;
        if(!promotion.isPresent()){
            throw new ResourceNotFoundException("The promotion is not found");
        }
        return promotion.get() ;
    }

    public Promotion findApplicablePromotion(String code, LocalDateTime now, Event event) {
        return promotionJpaRepository.findByCode(code)
                .filter(promotion -> promotion.isValidAt(now) && promotion.isApplicableTo(event))
                .orElseThrow(() -> new InvalidOperationException("Code promo invalide"));
    }

    public PromotionResponse update(UUID id , PromotionUpdateRequest request){
        Promotion promotion = findById(id) ;
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());
        promotion.setLimiteUtilisation(request.limiteUtilisation());
        promotion.setValeurReduction(request.valeurReduction());
        promotionJpaRepository.save(promotion) ;
        return promotionMapper.toResponse(promotion) ;
    }

    public void checkIsActive(Promotion promotion){
        if(!promotion.getActive()){
            throw new InvalidOperationException("La promotion est annulée") ;
        }
    }

    public void checkIsPossibleNow(Promotion promotion){
        LocalDateTime now = LocalDateTime.now() ;
        if(promotion.getStartDate().isAfter(now) || promotion.getEndDate().isBefore(now)){
            throw new InvalidOperationException("La promotion est pas possible a cette date") ;
        }
    }

    public void AddNewNumberOfUtilisation(Promotion promotion,Long numberOfUsers){
       if(promotion.getNombreUtilisations()+numberOfUsers <= promotion.getLimiteUtilisation()){
           promotion.addUtilisation(numberOfUsers);
       }
       else {
           throw new InvalidOperationException("La promotion est pas suffisant pour ce nombre") ;
       }
    }


}
