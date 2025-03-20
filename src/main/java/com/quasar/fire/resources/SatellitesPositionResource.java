package com.quasar.fire.resources;

import com.quasar.fire.model.dtos.CustomRequest;
import com.quasar.fire.services.impl.PositionCalculateServiceImpl;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/topsecret")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SatellitesPositionResource {
    private final PositionCalculateServiceImpl positionCalculateService;
    @Inject
    public SatellitesPositionResource(PositionCalculateServiceImpl positionCalculateService) {
        this.positionCalculateService = positionCalculateService;
    }

    @POST
    public Uni<Response> topSecret(@Valid CustomRequest request){
        return positionCalculateService.calculatePosition(request)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build);
    }
}
