package com.quasar.fire.infrastructure.adapter.in.rest;

import com.quasar.fire.domain.model.SatelliteSignal;
import com.quasar.fire.domain.model.SpacecraftLocation;
import com.quasar.fire.domain.port.in.LocateFromCacheUseCase;
import com.quasar.fire.domain.port.in.RegisterSignalUseCase;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.ErrorResponse;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.SatelliteSignalRequest;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.TrilaterationResponse;
import com.quasar.fire.infrastructure.adapter.in.rest.mapper.SignalMapper;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/topsecret_split")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Signal Registration", description = "Registro y consulta de senales de satelites en cache")
public class TopSecretSplitResource {

    private final RegisterSignalUseCase registerUseCase;
    private final LocateFromCacheUseCase locateFromCacheUseCase;
    private final long ttlSeconds;

    @Inject
    public TopSecretSplitResource(
            RegisterSignalUseCase registerUseCase,
            LocateFromCacheUseCase locateFromCacheUseCase,
            @ConfigProperty(name = "quasar.fire.constants.time.seconds.limit") long ttlSeconds
    ) {
        this.registerUseCase = registerUseCase;
        this.locateFromCacheUseCase = locateFromCacheUseCase;
        this.ttlSeconds = ttlSeconds;
    }

    @POST
    @Path("/{satellite_name}")
    @Operation(
            summary = "Registra una senal de satelite",
            description = "Almacena en cache la senal de un satelite individual con TTL configurable"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Senal registrada o recuperada del cache",
                    content = @Content(schema = @Schema(implementation = SatelliteSignalRequest.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Request invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @Counted(value = "topsecret_split.register.requests", description = "Total de senales registradas")
    @Timed(value = "topsecret_split.register.duration", description = "Duracion del registro de senal")
    public SatelliteSignalRequest registerSatellite(
            @Parameter(description = "Nombre del satelite", required = true, example = "kenobi")
            @PathParam("satellite_name")
            @NotBlank @Size(max = 50)
            String satelliteName,

            @Valid SatelliteSignalRequest request
    ) {
        SatelliteSignal signal = SignalMapper.toSignal(request, satelliteName);
        SatelliteSignal result = registerUseCase.register(signal, ttlSeconds);
        return SignalMapper.toSignalResponse(result);
    }

    @GET
    @Operation(
            summary = "Calcula posicion desde el cache",
            description = "Calcula la posicion de la nave usando las senales previamente registradas en el cache"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Posicion calculada exitosamente",
                    content = @Content(schema = @Schema(implementation = TrilaterationResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Satelites insuficientes en cache",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @Counted(value = "topsecret_split.locate.requests", description = "Total de calculos desde cache")
    @Timed(value = "topsecret_split.locate.duration", description = "Duracion del calculo desde cache")
    public TrilaterationResponse calculateFromCache() {
        SpacecraftLocation location = locateFromCacheUseCase.locate();
        return SignalMapper.toResponse(location);
    }
}
