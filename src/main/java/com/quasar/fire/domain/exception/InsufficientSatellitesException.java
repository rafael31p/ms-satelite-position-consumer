package com.quasar.fire.domain.exception;

public class InsufficientSatellitesException extends DomainException {

    public InsufficientSatellitesException(int count) {
        super("Se requieren 3 satelites, se recibieron: " + count);
    }
}
