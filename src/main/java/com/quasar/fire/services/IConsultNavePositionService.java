package com.quasar.fire.services;

import com.quasar.fire.model.dtos.SatelliteDistance;
import com.quasar.fire.model.dtos.cache.SatellitePositions;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface IConsultNavePositionService {
    Uni<SatellitePositions> getNavePositionByName(String nameSatellite);

    Uni<SatellitePositions> saveCacheNavePosition(SatelliteDistance satelliteDistance);

    Uni<List<SatellitePositions>> getAllNavePositionByName(List<String> nameSatellite);
}
