package com.quasar.fire.resources;

import com.quasar.fire.model.dtos.Satellite;
import com.quasar.fire.model.dtos.SatelliteDistance;
import com.quasar.fire.services.IPositionCalculateService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/topsecret_split")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PositionResources {
    private final IPositionCalculateService positionCalculateService;

    public PositionResources(IPositionCalculateService positionCalculateService) {
        this.positionCalculateService = positionCalculateService;
    }


    @POST
    @Path("/{satellite_name}")
    public Uni<Response> topSecretSplit(@PathParam("satellite_name") String satelliteName,
                                        @Valid Satellite satelliteDistance){
        return positionCalculateService.registerNavePosition(new SatelliteDistance(satelliteName, satelliteDistance.distance(), satelliteDistance.message()), satelliteName)
                .onItem().transformToUni(result->Uni.createFrom().item(Response.ok(result).build()));
    }

    @GET
    public Uni<Response> topSecretSplit(){
        return positionCalculateService.calculatePositionByCache()
                .onItem().transformToUni(result->Uni.createFrom().item(Response.ok(result).build()));
    }

}
