package com.quasar.fire.model.dtos;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Position(Double x,
                       Double y) {
}
