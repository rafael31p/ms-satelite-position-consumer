package com.quasar.fire.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasar.fire.domain.model.Distance;
import com.quasar.fire.domain.model.MessageFragment;
import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SatelliteSignal;
import com.quasar.fire.domain.port.out.SpacecraftSignalRepository;
import com.quasar.fire.infrastructure.adapter.out.persistence.entity.SpacecraftSignalEntity;
import com.quasar.fire.infrastructure.adapter.out.persistence.entity.SpacecraftSignalEntity.SatelliteDistanceEntity;
import io.quarkus.infinispan.client.Remote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class InfinispanSpacecraftSignalRepository implements SpacecraftSignalRepository {

    private static final Logger LOG = Logger.getLogger(InfinispanSpacecraftSignalRepository.class);

    private final RemoteCache<String, String> cache;
    private final ObjectMapper mapper;

    @Inject
    public InfinispanSpacecraftSignalRepository(
            @Remote("NAVE_POSITIONS") RemoteCache<String, String> cache,
            ObjectMapper mapper
    ) {
        this.cache = cache;
        this.mapper = mapper;
    }

    @Override
    @Retry(maxRetries = 3, delay = 200, jitter = 100)
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    public Optional<SatelliteSignal> findByName(SatelliteName name) {
        String json = cache.get(name.value());
        if (json == null) {
            return Optional.empty();
        }
        return Optional.of(deserialize(json));
    }

    @Override
    @Retry(maxRetries = 3, delay = 200, jitter = 100)
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    public List<SatelliteSignal> findByNames(List<SatelliteName> names) {
        return names.stream()
                .map(name -> cache.get(name.value()))
                .filter(Objects::nonNull)
                .map(this::deserialize)
                .toList();
    }

    @Override
    @Retry(maxRetries = 3, delay = 200, jitter = 100)
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    public SatelliteSignal save(SatelliteSignal signal) {
        try {
            SatelliteDistanceEntity distEntity = new SatelliteDistanceEntity(
                    signal.name().value(),
                    signal.distance().value(),
                    signal.message().words()
            );
            SpacecraftSignalEntity entity = new SpacecraftSignalEntity(distEntity, LocalDateTime.now());
            cache.put(signal.name().value(), mapper.writeValueAsString(entity));
            return signal;
        } catch (Exception e) {
            LOG.errorf("Error saving spacecraft signal: %s", e.getMessage());
            throw new RuntimeException("Error saving spacecraft signal", e);
        }
    }

    private SatelliteSignal deserialize(String json) {
        try {
            SpacecraftSignalEntity entity = mapper.readValue(json, SpacecraftSignalEntity.class);
            SatelliteDistanceEntity sd = entity.satelliteDistance();
            return new SatelliteSignal(
                    new SatelliteName(sd.name()),
                    new Distance(sd.distance()),
                    new MessageFragment(sd.message()),
                    entity.timestamp()
            );
        } catch (Exception e) {
            LOG.errorf("Error deserializing spacecraft signal: %s", e.getMessage());
            throw new RuntimeException("Error deserializing spacecraft signal", e);
        }
    }
}
