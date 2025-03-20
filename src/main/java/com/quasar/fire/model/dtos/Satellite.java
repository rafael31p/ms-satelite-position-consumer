package com.quasar.fire.model.dtos;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public record Satellite(@NotNull
                        Double distance,
                        @NotNull
                        String[] message) {
}
