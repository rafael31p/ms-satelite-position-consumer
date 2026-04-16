package com.quasar.fire.domain.model;

public record Coordinates(double x, double y) {

    public Coordinates {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            throw new IllegalArgumentException("Coordinates cannot be NaN: x=" + x + ", y=" + y);
        }
    }
}
