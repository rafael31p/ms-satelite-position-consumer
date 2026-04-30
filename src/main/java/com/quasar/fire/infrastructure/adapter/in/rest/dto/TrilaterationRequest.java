package com.quasar.fire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TrilaterationRequest(
        @NotNull(message = "La lista de satelites es obligatoria")
        @Size(min = 3, max = 3, message = "Se requieren exactamente 3 satelites")
        @Valid
        List<SatelliteSignalRequest> satellites
) {
}
