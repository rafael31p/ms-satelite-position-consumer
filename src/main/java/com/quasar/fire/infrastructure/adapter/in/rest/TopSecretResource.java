package com.quasar.fire.infrastructure.adapter.in.rest;

import com.quasar.fire.domain.model.Distance;
import com.quasar.fire.domain.model.MessageFragment;
import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SpacecraftLocation;
import com.quasar.fire.domain.port.in.LocateSpacecraftUseCase;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.TrilaterationRequest;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.TrilaterationResponse;
import com.quasar.fire.infrastructure.adapter.in.rest.mapper.SignalMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/topsecret")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TopSecretResource {

    private final LocateSpacecraftUseCase locateUseCase;

    @Inject
    public TopSecretResource(LocateSpacecraftUseCase locateUseCase) {
        this.locateUseCase = locateUseCase;
    }

    @POST
    public TrilaterationResponse topSecret(TrilaterationRequest request) {
        List<SatelliteName> names = SignalMapper.toNames(request);
        List<Distance> distances = SignalMapper.toDistances(request);
        List<MessageFragment> fragments = SignalMapper.toFragments(request);

        SpacecraftLocation location = locateUseCase.locate(names, distances, fragments);
        return SignalMapper.toResponse(location);
    }
}
