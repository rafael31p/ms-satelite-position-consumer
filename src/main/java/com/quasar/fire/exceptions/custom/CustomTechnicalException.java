package com.quasar.fire.exceptions.custom;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class CustomTechnicalException extends RuntimeException {
    public CustomTechnicalException(String message) {
        super(message);
    }
}
