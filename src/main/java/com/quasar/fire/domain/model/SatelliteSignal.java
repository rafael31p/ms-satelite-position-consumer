package com.quasar.fire.domain.model;

import java.time.Duration;
import java.time.LocalDateTime;

public record SatelliteSignal(
        SatelliteName name,
        Distance distance,
        MessageFragment message,
        LocalDateTime receivedAt
) {

    public SatelliteSignal {
        if (name == null) throw new IllegalArgumentException("Signal satellite name is required");
        if (distance == null) throw new IllegalArgumentException("Signal distance is required");
        if (message == null) throw new IllegalArgumentException("Signal message is required");
        if (receivedAt == null) throw new IllegalArgumentException("Signal receivedAt is required");
    }

    public boolean isExpired(long ttlSeconds) {
        return Duration.between(receivedAt, LocalDateTime.now()).getSeconds() > ttlSeconds;
    }
}
