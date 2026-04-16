package com.quasar.fire.domain.model;

public record SpacecraftLocation(Coordinates position, String message) {

    public SpacecraftLocation {
        if (position == null) throw new IllegalArgumentException("Position is required");
        if (message == null) throw new IllegalArgumentException("Message is required");
    }
}
