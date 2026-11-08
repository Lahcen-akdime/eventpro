package com.eventPro.api.Controller.Admin;

import com.eventPro.api.Dto.Remboursement.RemboursementResponse;
import com.eventPro.api.Entity.Enum.StatutRemboursement;
import com.eventPro.api.Service.RemboursementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/refunds")
@RequiredArgsConstructor
@Tag(name = "Admin Refunds")
public class AdminRemboursementController {

    private final RemboursementService remboursementService;

    @GetMapping
    public ResponseEntity<Page<RemboursementResponse>> getAllRefunds(

            HttpServletRequest httpServletResuest,

            @PageableDefault(
                    size = 20,
                    sort = "requestedAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable

    ) {

        return ResponseEntity.ok(
                remboursementService.fullList( pageable,httpServletResuest)
        );
    }
}
