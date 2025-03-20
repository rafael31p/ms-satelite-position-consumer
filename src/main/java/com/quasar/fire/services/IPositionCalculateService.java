package com.quasar.fire.services;

import com.quasar.fire.model.dtos.CustomRequest;
import com.quasar.fire.model.dtos.CustomResponse;
import com.quasar.fire.model.dtos.SatelliteDistance;
import io.smallrye.mutiny.Uni;

public interface IPositionCalculateService {
    Uni<CustomResponse> calculatePosition(CustomRequest request);

    Uni<SatelliteDistance> registerNavePosition(SatelliteDistance satelliteDistance, String nameSatellite);

    Uni<CustomResponse> calculatePositionByCache();
}