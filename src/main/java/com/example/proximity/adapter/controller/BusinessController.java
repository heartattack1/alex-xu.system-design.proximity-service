package com.example.proximity.adapter.controller;

import com.example.proximity.adapter.dto.BusinessRequest;
import com.example.proximity.adapter.dto.BusinessResponse;
import com.example.proximity.application.service.BusinessService;
import com.example.proximity.domain.Business;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/businesses")
public class BusinessController {
    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping
    public ResponseEntity<BusinessResponse> create(@Valid @RequestBody BusinessRequest request) {
        Business business = businessService.create(request.getName(), request.getLatitude(), request.getLongitude(),
                request.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(business));
    }

    @PutMapping("/{id}")
    public BusinessResponse update(@PathVariable UUID id, @Valid @RequestBody BusinessRequest request) {
        Business business = businessService.update(id, request.getName(), request.getLatitude(), request.getLongitude(),
                request.getAddress());
        return toResponse(business);
    }

    @GetMapping("/{id}")
    public BusinessResponse get(@PathVariable UUID id) {
        return toResponse(businessService.get(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        businessService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private BusinessResponse toResponse(Business business) {
        return new BusinessResponse(business.getId(), business.getName(), business.getLatitude(),
                business.getLongitude(), business.getAddress());
    }
}
