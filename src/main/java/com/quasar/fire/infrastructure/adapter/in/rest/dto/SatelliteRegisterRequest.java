package com.quasar.fire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SatelliteRegisterRequest(
        @PositiveOrZero(message = "La distancia debe ser cero o positiva")
        double distance,

        @NotNull(message = "El mensaje es obligatorio")
        @Size(max = 100, message = "El mensaje no puede exceder 100 fragmentos")
        String[] message
) {
}
