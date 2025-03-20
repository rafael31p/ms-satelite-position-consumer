package com.quasar.fire.services.impl;

import com.quasar.fire.exceptions.custom.CustomFuntionalException;
import com.quasar.fire.model.dtos.CustomRequest;
import com.quasar.fire.model.dtos.CustomResponse;
import com.quasar.fire.model.dtos.Position;
import com.quasar.fire.model.dtos.SatelliteDistance;
import com.quasar.fire.model.dtos.cache.Satellite;
import com.quasar.fire.model.dtos.cache.SatellitePositions;
import com.quasar.fire.services.IConsultNavePositionService;
import com.quasar.fire.services.IConsultSatelliteService;
import com.quasar.fire.services.IPositionCalculateService;
import com.quasar.fire.utils.CalculatePositionAndGenerateMessageUtil;
import com.quasar.fire.utils.Constants;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class PositionCalculateServiceImpl implements IPositionCalculateService {
    private static final Logger LOGGER = Logger.getLogger(PositionCalculateServiceImpl.class);

    private final IConsultSatelliteService consultSatelliteService;
    private final IConsultNavePositionService consultNavePositionService;
    @ConfigProperty(name = "quasar.fire.message.error.funtional.cache")
    String messageErrorFuntional;
    @ConfigProperty(name = "quasar.fire.constants.time.seconds.limit")
    Long timeSecondsLimit;
    @ConfigProperty(name = "quasar.fire.constants.name.satellites")
    String satellites;

    @Inject
    public PositionCalculateServiceImpl(IConsultSatelliteService consultSatelliteService, IConsultNavePositionService consultNavePositionService) {
        this.consultSatelliteService = consultSatelliteService;
        this.consultNavePositionService = consultNavePositionService;
    }
    @Override
    public Uni<CustomResponse> calculatePosition(CustomRequest request) {
        String message = CalculatePositionAndGenerateMessageUtil.getMessage(request.satellites().stream()
                .map(SatelliteDistance::message)
                .toList());
        List<String> namesSatellites = request.satellites().stream()
                .map(SatelliteDistance::name)
                .toList();
        return consultSatelliteService.getAllSatellitesByName(namesSatellites)
                .flatMap(satellite -> {
                    List<Position> positions = this.extractPositions(satellite);
                    List<Double> distances = request.satellites().stream()
                            .sorted(Comparator.comparing(SatelliteDistance::name))
                            .map(SatelliteDistance::distance)
                            .toList();
                    if(positions.size() <3 || distances.size() < 3) {
                        LOGGER.errorf("No se puede calcular la posición, no hay suficientes satélites o distancias");
                        throw new CustomFuntionalException(messageErrorFuntional.replace(Constants.VALUE, namesSatellites.toString()));
                    }
                    LOGGER.infof("Distances: %s", distances.toString());
                    LOGGER.infof("Positions: %s", positions.toString());
                    Position position = CalculatePositionAndGenerateMessageUtil.calculatePositionByOrder(positions, distances);
                    return Uni.createFrom().item(new CustomResponse(position, message));
                });
    }
    @Override
    public Uni<SatelliteDistance> registerNavePosition(SatelliteDistance satelliteDistance, String nameSatellite) {
        return consultNavePositionService.getNavePositionByName(nameSatellite).flatMap(result -> {
            if(result == null) {
                return Uni.createFrom().nullItem();
            }
            LocalDateTime now = LocalDateTime.now();
            Long timeSeconds = Duration.between(result.timestamp(), now).getSeconds();
            if(timeSecondsLimit > timeSeconds) {
                LOGGER.infof("La posición de la nave ha expirado, se guardará en cache");
                return Uni.createFrom().item(result.satelliteDistance());
            }else {
                return Uni.createFrom().nullItem();
            }
        }).onItem().ifNull().switchTo(()-> {
            LOGGER.infof("No se encontró la posición de la nave, se guardará en cache");
            return consultNavePositionService.saveCacheNavePosition(satelliteDistance)
                    .map(SatellitePositions::satelliteDistance);
        });
    }
    @Override
    public Uni<CustomResponse> calculatePositionByCache(){
        List<String> namesSatellites = List.of(satellites.split(","));
        Uni<List<SatellitePositions>> navePositions = consultNavePositionService.getAllNavePositionByName(namesSatellites);
        Uni<List<Double>> distance = navePositions.map(this::extractDistances);
        Uni<String> message = navePositions.map(this::extractMessage);
        Uni<List<Position>> positions = consultSatelliteService.getAllSatellitesByName(namesSatellites)
                .map(this::extractPositions);
        return Uni.combine().all().unis(distance, positions, message)
                .asTuple()
                .map(tuple -> {
                    List<Double> distances = tuple.getItem1();
                    List<Position> positions1 = tuple.getItem2();
                    String messageFinal = tuple.getItem3();
                    if(distances.size()<3 || positions1.size()<3){
                        throw new CustomFuntionalException(messageErrorFuntional.replace(Constants.VALUE, namesSatellites.toString()));
                    }
                    Position position = CalculatePositionAndGenerateMessageUtil.calculatePositionByOrder(positions1, distances);
                    return new CustomResponse(position, messageFinal);
                });
    }
    private List<Double> extractDistances(List<SatellitePositions> positions) {
        return positions.stream()
                .map(SatellitePositions::satelliteDistance)
                .sorted(Comparator.comparing(SatelliteDistance::name))
                .map(SatelliteDistance::distance)
                .toList();
    }

    private String extractMessage(List<SatellitePositions> positions) {
        List<String[]> messages = positions.stream()
                .map(SatellitePositions::satelliteDistance)
                .map(SatelliteDistance::message)
                .toList();
        return CalculatePositionAndGenerateMessageUtil.getMessage(messages);
    }

    private List<Position> extractPositions(List<Satellite> satellites) {
        return satellites.stream()
                .sorted(Comparator.comparing(Satellite::name))
                .map(Satellite::position)
                .toList();
    }

}