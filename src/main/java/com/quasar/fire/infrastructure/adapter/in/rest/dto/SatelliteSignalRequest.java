package com.quasar.fire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SatelliteSignalRequest(
        @NotBlank(message = "El nombre del satelite es obligatorio")
        @Size(max = 50, message = "El nombre del satelite no puede exceder 50 caracteres")
        String name,

        @PositiveOrZero(message = "La distancia debe ser cero o positiva")
        double distance,

        @NotNull(message = "El mensaje es obligatorio")
        @Size(max = 100, message = "El mensaje no puede exceder 100 fragmentos")
        String[] message
) {
}
