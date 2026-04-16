package com.quasar.fire.infrastructure.adapter.out.persistence.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record SatelliteStationEntity(String name, PositionEntity position) {

    @RegisterForReflection
    public record PositionEntity(double x, double y) {
    }
}
