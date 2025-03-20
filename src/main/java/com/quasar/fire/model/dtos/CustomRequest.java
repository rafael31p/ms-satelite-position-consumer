package com.quasar.fire.model.dtos;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@RegisterForReflection
public record CustomRequest(@NotNull
                            List<SatelliteDistance> satellites) {
}
