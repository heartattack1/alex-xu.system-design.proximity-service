package com.example.proximity.infrastructure.repository;

import com.example.proximity.domain.Business;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
}
