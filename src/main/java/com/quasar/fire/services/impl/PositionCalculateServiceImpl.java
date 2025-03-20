package com.quasar.fire.services.impl;

import com.quasar.fire.exceptions.custom.CustomFuntionalException;
import com.quasar.fire.model.dtos.CustomRequest;
import com.quasar.fire.model.dtos.CustomResponse;
import com.quasar.fire.model.dtos.Position;
import com.quasar.fire.model.dtos.SatelliteDistance;
import com.quasar.fire.model.dtos.cache.Satellite;
import com.quasar.fire.services.IConsultSatelliteService;
import com.quasar.fire.services.IPositionCalculateService;
import com.quasar.fire.utils.CalculatePositionAndGenerateMessageUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class PositionCalculateServiceImpl implements IPositionCalculateService {
    private static final Logger LOGGER = Logger.getLogger(PositionCalculateServiceImpl.class);

    private final IConsultSatelliteService consultSatelliteService;
    @ConfigProperty(name = "quasar.fire.message.error.funtional.cache")
    String messageErrorFuntional;

    @Inject
    public PositionCalculateServiceImpl(IConsultSatelliteService consultSatelliteService) {
        this.consultSatelliteService = consultSatelliteService;
    }
    @Override
    public Uni<CustomResponse> calculatePosition(CustomRequest request) {
        String message = CalculatePositionAndGenerateMessageUtil.getMessage(request.satellites().stream()
                .map(SatelliteDistance::message)
                .toList());
        List<String> namesSatellites = request.satellites().stream()
                .map(SatelliteDistance::name)
                .toList();
        return consultSatelliteService.getSatellitesByName(namesSatellites)
                .flatMap(satellite -> {
                    List<Position> positions = satellite.stream()
                            .sorted(Comparator.comparing(Satellite::name))
                            .map(Satellite::position)
                            .toList();
                    List<Double> distances = request.satellites().stream()
                            .sorted(Comparator.comparing(SatelliteDistance::name))
                            .map(SatelliteDistance::distance)
                            .toList();
                    if(positions.size() <3 || distances.size() < 3) {
                        LOGGER.errorf("No se puede calcular la posición, no hay suficientes satélites o distancias");
                        throw new CustomFuntionalException(messageErrorFuntional.replace("value", namesSatellites.toString()));
                    }
                    LOGGER.infof("Distances: %s", distances.toString());
                    LOGGER.infof("Positions: %s", positions.toString());
                    Position position = CalculatePositionAndGenerateMessageUtil.calculatePositionByOrder(positions, distances);
                    return Uni.createFrom().item(new CustomResponse(position, message));
                });
    }

}