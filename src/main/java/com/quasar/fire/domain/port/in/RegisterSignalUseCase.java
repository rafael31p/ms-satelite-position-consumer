package com.quasar.fire.domain.port.in;

import com.quasar.fire.domain.model.SatelliteSignal;

public interface RegisterSignalUseCase {

    SatelliteSignal register(SatelliteSignal signal, long ttlSeconds);
}
