package com.quasar.fire.services;

import com.quasar.fire.model.dtos.cache.Satellite;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface IConsultSatelliteService {
    Uni<List<Satellite>> getSatellitesByName(List<String> nameSatellites);
}
