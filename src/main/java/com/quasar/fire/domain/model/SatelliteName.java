package com.quasar.fire.domain.model;

public record SatelliteName(String value) implements Comparable<SatelliteName> {

    public SatelliteName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Satellite name cannot be null or blank");
        }
    }

    @Override
    public int compareTo(SatelliteName other) {
        return this.value.compareTo(other.value);
    }
}
