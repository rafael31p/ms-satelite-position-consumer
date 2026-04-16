package com.quasar.fire.infrastructure.adapter.out.persistence.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;

@RegisterForReflection
public record SpacecraftSignalEntity(SatelliteDistanceEntity satelliteDistance, LocalDateTime timestamp) {

    @RegisterForReflection
    public record SatelliteDistanceEntity(String name, double distance, String[] message) {
    }
}
