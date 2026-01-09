package com.example.proximity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "geo_index")
public class GeoIndex {
    @Id
    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(nullable = false)
    private String geohash;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    protected GeoIndex() {
    }

    public GeoIndex(UUID businessId, String geohash, double latitude, double longitude) {
        this.businessId = businessId;
        this.geohash = geohash;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public void setBusinessId(UUID businessId) {
        this.businessId = businessId;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
