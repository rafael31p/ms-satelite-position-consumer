package com.quasar.fire.infrastructure.adapter.in.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ErrorResponse(int status, String message) {
}
