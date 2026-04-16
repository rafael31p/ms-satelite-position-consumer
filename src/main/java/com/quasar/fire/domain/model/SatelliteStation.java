package com.quasar.fire.domain.model;

public record SatelliteStation(SatelliteName name, Coordinates position) {

    public SatelliteStation {
        if (name == null) throw new IllegalArgumentException("Station name is required");
        if (position == null) throw new IllegalArgumentException("Station position is required");
    }
}
