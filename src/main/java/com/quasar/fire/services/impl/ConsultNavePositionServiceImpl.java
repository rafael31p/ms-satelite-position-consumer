package com.quasar.fire.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasar.fire.exceptions.custom.CustomFuntionalException;
import com.quasar.fire.exceptions.custom.CustomTechnicalException;
import com.quasar.fire.model.dtos.SatelliteDistance;
import com.quasar.fire.model.dtos.cache.SatellitePositions;
import com.quasar.fire.services.IConsultNavePositionService;
import com.quasar.fire.utils.Constants;
import io.quarkus.infinispan.client.Remote;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ConsultNavePositionServiceImpl implements IConsultNavePositionService {
    @ConfigProperty(name = "quasar.fire.message.error.funtional.cache", defaultValue = "No se encontraron satelites value")
    String messageErrorFuntional;
    @ConfigProperty(name = "quasar.fire.message.error.techinal.cache", defaultValue = "Error al procesar el valor del satelite value")
    String messageErrorTechnical;
    private final RemoteCache<String, String> cacheNave;
    private final ObjectMapper mapper;

    @Inject
    public ConsultNavePositionServiceImpl(@Remote("NAVE_POSITIONS")
                                          RemoteCache<String, String> cacheNave,
                                          ObjectMapper mapper) {
        this.cacheNave = cacheNave;
        this.mapper = mapper;
    }
    @Override
    public Uni<SatellitePositions> getNavePositionByName(String nameSatellite) {
        return Uni.createFrom().item(() -> cacheNave.get(nameSatellite))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .flatMap(result -> {
                    try {
                        return result == null ? Uni.createFrom().nullItem() : Uni.createFrom().item(mapper.readValue(result, SatellitePositions.class));
                    } catch (Exception e) {
                        throw new CustomTechnicalException(messageErrorTechnical.replace(Constants.VALUE, e.getMessage()));
                    }
                });
    }
    @Override
    public Uni<SatellitePositions> saveCacheNavePosition(SatelliteDistance satelliteDistance) {
        return Uni.createFrom().item(() -> {
            try {
                SatellitePositions satellitePositions = new SatellitePositions(
                        satelliteDistance,
                        LocalDateTime.now()
                );
                cacheNave.put(satelliteDistance.name(), mapper.writeValueAsString(satellitePositions));
                return satellitePositions;
            } catch (Exception e) {
                throw new CustomTechnicalException(messageErrorTechnical.replace(Constants.VALUE, e.getMessage()));
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .onItem().ifNull().failWith(
                () -> new CustomTechnicalException(messageErrorTechnical.replace(Constants.VALUE, satelliteDistance.toString()))
        );
    }
    @Override
    public Uni<List<SatellitePositions>> getAllNavePositionByName(List<String> nameSatellite) {
        return Uni.createFrom().item(() ->
                nameSatellite
                        .stream()
                        .map(cacheNave::get)
                        .filter(Objects::nonNull)
                        .map(result -> {
                            try {
                                return mapper.readValue(result, SatellitePositions.class);
                            } catch (Exception e) {
                                throw new CustomTechnicalException(messageErrorTechnical.replace(Constants.VALUE, e.getMessage()));
                            }
                        })
                        .toList()
        ).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .onItem().ifNull().failWith(
                () -> new CustomFuntionalException(messageErrorFuntional.replace(Constants.VALUE, nameSatellite.toString()))
        );
    }
}
