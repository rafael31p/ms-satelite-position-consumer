package com.quasar.fire.model.dtos;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record CustomResposeError(Integer code,
                                  String message) {
}
