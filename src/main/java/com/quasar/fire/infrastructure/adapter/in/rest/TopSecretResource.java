package com.quasar.fire.infrastructure.adapter.in.rest;

import com.quasar.fire.domain.model.Distance;
import com.quasar.fire.domain.model.MessageFragment;
import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SpacecraftLocation;
import com.quasar.fire.domain.port.in.LocateSpacecraftUseCase;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.ErrorResponse;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.TrilaterationRequest;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.TrilaterationResponse;
import com.quasar.fire.infrastructure.adapter.in.rest.mapper.SignalMapper;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/topsecret")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Trilateration", description = "Calculo de posicion de la nave a partir de senales de satelites")
public class TopSecretResource {

    private final LocateSpacecraftUseCase locateUseCase;

    @Inject
    public TopSecretResource(LocateSpacecraftUseCase locateUseCase) {
        this.locateUseCase = locateUseCase;
    }

    @POST
    @Operation(
            summary = "Calcula la posicion de la nave",
            description = "Recibe distancias y fragmentos de mensaje de 3 satelites y retorna la posicion calculada y el mensaje reconstruido"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Posicion calculada exitosamente",
                    content = @Content(schema = @Schema(implementation = TrilaterationResponse.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Request invalido (validacion de datos)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Satelites insuficientes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "No existe interseccion unica entre los circulos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @Counted(value = "topsecret.requests", description = "Total de requests al endpoint /topsecret")
    @Timed(value = "topsecret.duration", description = "Duracion de calculo en /topsecret")
    public TrilaterationResponse topSecret(@Valid TrilaterationRequest request) {
        List<SatelliteName> names = SignalMapper.toNames(request);
        List<Distance> distances = SignalMapper.toDistances(request);
        List<MessageFragment> fragments = SignalMapper.toFragments(request);

        SpacecraftLocation location = locateUseCase.locate(names, distances, fragments);
        return SignalMapper.toResponse(location);
    }
}
