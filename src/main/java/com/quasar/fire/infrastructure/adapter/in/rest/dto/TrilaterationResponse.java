package com.quasar.fire.infrastructure.adapter.in.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TrilaterationResponse(PositionDto position, String message) {

    @RegisterForReflection
    public record PositionDto(double x, double y) {
    }
}
