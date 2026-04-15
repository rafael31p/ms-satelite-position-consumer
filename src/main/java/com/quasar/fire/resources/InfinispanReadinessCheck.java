package com.quasar.fire.resources;

import io.quarkus.infinispan.client.Remote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.infinispan.client.hotrod.RemoteCache;

@Readiness
@ApplicationScoped
public class InfinispanReadinessCheck implements HealthCheck {

    private final RemoteCache<String, String> cacheSatellites;

    @Inject
    public InfinispanReadinessCheck(@Remote("SATELLITE_POSITIONS") RemoteCache<String, String> cacheSatellites) {
        this.cacheSatellites = cacheSatellites;
    }

    @Override
    public HealthCheckResponse call() {
        try {
            cacheSatellites.size();
            return HealthCheckResponse.up("infinispan-connection");
        } catch (Exception e) {
            return HealthCheckResponse.named("infinispan-connection")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
