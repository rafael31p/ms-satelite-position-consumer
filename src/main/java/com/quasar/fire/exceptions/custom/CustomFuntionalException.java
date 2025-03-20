package com.quasar.fire.exceptions.custom;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class CustomFuntionalException extends RuntimeException {
    public CustomFuntionalException(String message) {
        super(message);
    }
}
