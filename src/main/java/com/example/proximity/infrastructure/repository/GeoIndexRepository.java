package com.example.proximity.infrastructure.repository;

import com.example.proximity.domain.GeoIndex;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeoIndexRepository extends JpaRepository<GeoIndex, UUID> {
    List<GeoIndex> findByGeohashStartingWith(String geohashPrefix);
}
