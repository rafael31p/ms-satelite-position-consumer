package com.quasar.fire.domain.port.out;

import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SatelliteStation;

import java.util.List;

public interface SatelliteStationRepository {

    List<SatelliteStation> findByNames(List<SatelliteName> names);
}
