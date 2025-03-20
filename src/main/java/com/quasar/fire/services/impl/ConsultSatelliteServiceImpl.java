package com.quasar.fire.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasar.fire.exceptions.custom.CustomFuntionalException;
import com.quasar.fire.exceptions.custom.CustomTechnicalException;
import com.quasar.fire.model.dtos.cache.Satellite;
import com.quasar.fire.services.IConsultSatelliteService;
import com.quasar.fire.utils.Constants;
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
    private final RemoteCache<String, String> cacheSatellites;
    private final ObjectMapper mapper;

    @ConfigProperty(name = "quasar.fire.message.error.funtional.cache", defaultValue = "No se encontraron satelites value")
    String messageErrorFuntional;
    @ConfigProperty(name = "quasar.fire.message.error.techinal.cache", defaultValue = "Error al procesar el valor del satelite value")
    String messageErrorTechnical;
    @Inject
    public ConsultSatelliteServiceImpl(@Remote("SATELLITE_POSITIONS")
                                           RemoteCache<String, String> cacheSatellites,
                                       ObjectMapper mapper) {
        this.cacheSatellites = cacheSatellites;
        this.mapper = mapper;
    }

    @Override
    public Uni<List<Satellite>> getAllSatellitesByName(List<String> nameSatellites) {
        return Uni.createFrom().item(nameSatellites.stream()
                        .map(cacheSatellites::get)
                        .filter(Objects::nonNull)
                        .map(value -> {
                            try {
                                return mapper.readValue(value, Satellite.class);
                            } catch (Exception e) {
                                throw new CustomTechnicalException(messageErrorTechnical.replace(Constants.VALUE, value));
                            }
                        })
                        .toList()
                )
                .onItem().ifNull().failWith(()-> new CustomFuntionalException(messageErrorFuntional.replace(Constants.VALUE, nameSatellites.toString())));
    }
}
