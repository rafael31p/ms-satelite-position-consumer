package com.quasar.fire.model.dtos.cache;

import com.quasar.fire.model.dtos.Position;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Satellite(String name,
                        Position position) {
}
