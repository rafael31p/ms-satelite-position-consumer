package com.quasar.fire.domain.exception;

import java.util.List;

public class SignalNotFoundException extends DomainException {

    public SignalNotFoundException(List<String> satelliteNames) {
        super("No se encontraron senales para los satelites: " + satelliteNames);
    }
}
