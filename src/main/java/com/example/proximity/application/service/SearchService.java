package com.example.proximity.application.service;

import com.example.proximity.application.util.DistanceUtils;
import com.example.proximity.application.util.GeoHashUtils;
import com.example.proximity.domain.Business;
import com.example.proximity.domain.GeoIndex;
import com.example.proximity.infrastructure.repository.BusinessRepository;
import com.example.proximity.infrastructure.repository.GeoIndexRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private final GeoIndexRepository geoIndexRepository;
    private final BusinessRepository businessRepository;

    public SearchService(GeoIndexRepository geoIndexRepository, BusinessRepository businessRepository) {
        this.geoIndexRepository = geoIndexRepository;
        this.businessRepository = businessRepository;
    }

    public NearbySearchResult search(double latitude, double longitude, int radiusMeters) {
        int geohashLength = resolveGeohashLength(radiusMeters);
        String geohash = GeoHashUtils.encode(latitude, longitude, geohashLength);
        List<String> searchHashes = GeoHashUtils.neighbors(geohash);

        Map<UUID, GeoIndex> candidates = new HashMap<>();
        for (String hash : searchHashes) {
            for (GeoIndex index : geoIndexRepository.findByGeohashStartingWith(hash)) {
                candidates.put(index.getBusinessId(), index);
            }
        }

        List<UUID> ids = new ArrayList<>(candidates.keySet());
        Map<UUID, Business> businesses = businessRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Business::getId, business -> business));

        List<NearbyBusiness> results = new ArrayList<>();
        for (GeoIndex index : candidates.values()) {
            Business business = businesses.get(index.getBusinessId());
            if (business == null) {
                continue;
            }
            double distance = DistanceUtils.haversineMeters(latitude, longitude, index.getLatitude(), index.getLongitude());
            if (distance <= radiusMeters) {
                results.add(new NearbyBusiness(business.getId(), business.getName(), business.getLatitude(),
                        business.getLongitude(), distance));
            }
        }

        results.sort(Comparator.comparingDouble(NearbyBusiness::distanceMeters));
        return new NearbySearchResult(results.size(), results);
    }

    private int resolveGeohashLength(int radiusMeters) {
        if (radiusMeters <= 1000) {
            return 6;
        }
        if (radiusMeters <= 5000) {
            return 5;
        }
        return 4;
    }

    public record NearbyBusiness(UUID id, String name, double latitude, double longitude, double distanceMeters) {
    }

    public record NearbySearchResult(int total, List<NearbyBusiness> businesses) {
    }
}
