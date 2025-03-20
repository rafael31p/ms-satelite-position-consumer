package com.quasar.fire.model.dtos.cache;

import com.quasar.fire.model.dtos.SatelliteDistance;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;

@RegisterForReflection
public record SatellitePositions(SatelliteDistance satelliteDistance,
                                 LocalDateTime timestamp) {
}
