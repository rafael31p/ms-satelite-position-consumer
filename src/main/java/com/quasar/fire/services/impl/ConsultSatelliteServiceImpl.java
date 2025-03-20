package com.quasar.fire.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasar.fire.exceptions.custom.CustomFuntionalException;
import com.quasar.fire.exceptions.custom.CustomTechnicalException;
import com.quasar.fire.model.dtos.cache.Satellite;
import com.quasar.fire.services.IConsultSatelliteService;
import io.quarkus.infinispan.client.Remote;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCache;

import java.util.List;
import java.util.Objects;


@ApplicationScoped
public class ConsultSatelliteServiceImpl implements IConsultSatelliteService {
    @Inject
    @Remote("SATELLITE_POSITIONS")
    RemoteCache<String, String> cache;
    @Inject
    ObjectMapper mapper;

    @ConfigProperty(name = "quasar.fire.message.error.funtional.cache")
    String messageErrorFuntional;

    @Override
    public Uni<List<Satellite>> getSatellitesByName(List<String> nameSatellites) {
        return Uni.createFrom().item(nameSatellites.stream()
                                .map(name -> cache.get(name))
                                .filter(Objects::nonNull)
                                .map(value -> {
                                    try {
                                        return mapper.readValue(value, Satellite.class);
                                    } catch (Exception e) {
                                        throw new CustomTechnicalException(messageErrorFuntional.replace("value", value));
                                    }
                                })
                                .toList()
                )
                .onItem().ifNull().failWith(()-> new CustomFuntionalException(messageErrorFuntional.replace("value", nameSatellites.toString())));
    }
}
