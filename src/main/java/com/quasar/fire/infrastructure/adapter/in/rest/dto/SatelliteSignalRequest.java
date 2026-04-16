package com.quasar.fire.infrastructure.adapter.in.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record SatelliteSignalRequest(String name, double distance, String[] message) {
}
