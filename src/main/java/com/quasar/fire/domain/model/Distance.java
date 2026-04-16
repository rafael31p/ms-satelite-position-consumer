package com.quasar.fire.domain.model;

public record Distance(double value) {

    public Distance {
        if (value < 0) {
            throw new IllegalArgumentException("Distance cannot be negative: " + value);
        }
    }
}
