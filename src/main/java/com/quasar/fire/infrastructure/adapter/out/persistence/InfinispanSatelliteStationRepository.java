package com.quasar.fire.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasar.fire.domain.model.Coordinates;
import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SatelliteStation;
import com.quasar.fire.domain.port.out.SatelliteStationRepository;
import com.quasar.fire.infrastructure.adapter.out.persistence.entity.SatelliteStationEntity;
import io.quarkus.infinispan.client.Remote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class InfinispanSatelliteStationRepository implements SatelliteStationRepository {

    private static final Logger LOG = Logger.getLogger(InfinispanSatelliteStationRepository.class);

    private final RemoteCache<String, String> cache;
    private final ObjectMapper mapper;

    @Inject
    public InfinispanSatelliteStationRepository(
            @Remote("SATELLITE_POSITIONS") RemoteCache<String, String> cache,
            ObjectMapper mapper
    ) {
        this.cache = cache;
        this.mapper = mapper;
    }

    @Override
    public List<SatelliteStation> findByNames(List<SatelliteName> names) {
        return names.stream()
                .map(name -> cache.get(name.value()))
                .filter(Objects::nonNull)
                .map(this::deserialize)
                .toList();
    }

    private SatelliteStation deserialize(String json) {
        try {
            SatelliteStationEntity entity = mapper.readValue(json, SatelliteStationEntity.class);
            return new SatelliteStation(
                    new SatelliteName(entity.name()),
                    new Coordinates(entity.position().x(), entity.position().y())
            );
        } catch (Exception e) {
            LOG.errorf("Error deserializing satellite station: %s", e.getMessage());
            throw new RuntimeException("Error deserializing satellite station", e);
        }
    }
}
