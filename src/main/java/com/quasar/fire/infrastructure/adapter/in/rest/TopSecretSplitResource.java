package com.quasar.fire.infrastructure.adapter.in.rest;

import com.quasar.fire.domain.model.SatelliteSignal;
import com.quasar.fire.domain.model.SpacecraftLocation;
import com.quasar.fire.domain.port.in.LocateFromCacheUseCase;
import com.quasar.fire.domain.port.in.RegisterSignalUseCase;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.SatelliteSignalRequest;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.TrilaterationResponse;
import com.quasar.fire.infrastructure.adapter.in.rest.mapper.SignalMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/topsecret_split")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TopSecretSplitResource {

    private final RegisterSignalUseCase registerUseCase;
    private final LocateFromCacheUseCase locateFromCacheUseCase;

    @ConfigProperty(name = "quasar.fire.constants.time.seconds.limit")
    long ttlSeconds;

    @Inject
    public TopSecretSplitResource(
            RegisterSignalUseCase registerUseCase,
            LocateFromCacheUseCase locateFromCacheUseCase
    ) {
        this.registerUseCase = registerUseCase;
        this.locateFromCacheUseCase = locateFromCacheUseCase;
    }

    @POST
    @Path("/{satellite_name}")
    public SatelliteSignalRequest registerSatellite(
            @PathParam("satellite_name") String satelliteName,
            SatelliteSignalRequest request
    ) {
        SatelliteSignal signal = SignalMapper.toSignal(request, satelliteName);
        SatelliteSignal result = registerUseCase.register(signal, ttlSeconds);
        return SignalMapper.toSignalResponse(result);
    }

    @GET
    public TrilaterationResponse calculateFromCache() {
        SpacecraftLocation location = locateFromCacheUseCase.locate();
        return SignalMapper.toResponse(location);
    }
}
