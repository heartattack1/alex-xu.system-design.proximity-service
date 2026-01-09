package com.example.proximity.application.service;

import com.example.proximity.application.util.GeoHashUtils;
import com.example.proximity.domain.Business;
import com.example.proximity.domain.GeoIndex;
import com.example.proximity.infrastructure.repository.BusinessRepository;
import com.example.proximity.infrastructure.repository.GeoIndexRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BusinessService {
    private static final int GEOHASH_LENGTH = 6;

    private final BusinessRepository businessRepository;
    private final GeoIndexRepository geoIndexRepository;

    public BusinessService(BusinessRepository businessRepository, GeoIndexRepository geoIndexRepository) {
        this.businessRepository = businessRepository;
        this.geoIndexRepository = geoIndexRepository;
    }

    @Transactional
    public Business create(String name, double latitude, double longitude, String address) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        Business business = new Business(id, name, latitude, longitude, address, now);
        businessRepository.save(business);
        updateGeoIndex(business);
        return business;
    }

    @Transactional
    public Business update(UUID id, String name, double latitude, double longitude, String address) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        business.setName(name);
        business.setLatitude(latitude);
        business.setLongitude(longitude);
        business.setAddress(address);
        business.setUpdatedAt(OffsetDateTime.now());
        businessRepository.save(business);
        updateGeoIndex(business);
        return business;
    }

    @Transactional
    public void delete(UUID id) {
        if (!businessRepository.existsById(id)) {
            throw new ResourceNotFoundException("Business not found");
        }
        geoIndexRepository.deleteById(id);
        businessRepository.deleteById(id);
    }

    public Business get(UUID id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
    }

    private void updateGeoIndex(Business business) {
        String geohash = GeoHashUtils.encode(business.getLatitude(), business.getLongitude(), GEOHASH_LENGTH);
        GeoIndex geoIndex = new GeoIndex(business.getId(), geohash, business.getLatitude(), business.getLongitude());
        geoIndexRepository.save(geoIndex);
    }
}
