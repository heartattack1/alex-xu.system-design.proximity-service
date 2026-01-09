package com.example.proximity.adapter.controller;

import com.example.proximity.adapter.dto.NearbySearchResponse;
import com.example.proximity.application.service.SearchService;
import com.example.proximity.application.service.SearchService.NearbyBusiness;
import com.example.proximity.application.service.SearchService.NearbySearchResult;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/v1/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/nearby")
    public NearbySearchResponse nearby(@RequestParam @NotNull Double latitude,
                                       @RequestParam @NotNull Double longitude,
                                       @RequestParam(defaultValue = "5000") @Min(1) @Max(20000) Integer radius) {
        NearbySearchResult result = searchService.search(latitude, longitude, radius);
        List<NearbySearchResponse.NearbyBusinessResponse> businesses = result.businesses().stream()
                .map(this::toResponse)
                .toList();
        return new NearbySearchResponse(result.total(), businesses);
    }

    private NearbySearchResponse.NearbyBusinessResponse toResponse(NearbyBusiness business) {
        return new NearbySearchResponse.NearbyBusinessResponse(business.id(), business.name(), business.latitude(),
                business.longitude(), business.distanceMeters());
    }
}
