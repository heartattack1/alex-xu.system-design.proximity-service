package com.example.proximity.adapter.dto;

import java.util.UUID;

public record BusinessResponse(UUID id, String name, double latitude, double longitude, String address) {
}
