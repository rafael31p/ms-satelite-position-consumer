package com.quasar.fire.domain.port.out;

import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SatelliteSignal;

import java.util.List;
import java.util.Optional;

public interface SpacecraftSignalRepository {

    Optional<SatelliteSignal> findByName(SatelliteName name);

    List<SatelliteSignal> findByNames(List<SatelliteName> names);

    SatelliteSignal save(SatelliteSignal signal);
}
