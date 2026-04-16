package com.quasar.fire.infrastructure.adapter.in.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record TrilaterationRequest(List<SatelliteSignalRequest> satellites) {
}
