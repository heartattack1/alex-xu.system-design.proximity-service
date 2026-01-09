package com.example.proximity.adapter.dto;

import java.util.List;
import java.util.UUID;

public record NearbySearchResponse(int total, List<NearbyBusinessResponse> businesses) {
    public record NearbyBusinessResponse(UUID id, String name, double latitude, double longitude, double distanceMeters) {
    }
}
