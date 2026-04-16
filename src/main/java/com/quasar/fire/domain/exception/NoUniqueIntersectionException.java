package com.quasar.fire.domain.exception;

public class NoUniqueIntersectionException extends DomainException {

    public NoUniqueIntersectionException() {
        super("No existe interseccion unica entre los tres circulos");
    }
}
