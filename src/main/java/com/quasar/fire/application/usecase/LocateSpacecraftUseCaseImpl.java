package com.quasar.fire.application.usecase;

import com.quasar.fire.domain.model.Coordinates;
import com.quasar.fire.domain.model.Distance;
import com.quasar.fire.domain.model.MessageFragment;
import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SatelliteStation;
import com.quasar.fire.domain.model.SpacecraftLocation;
import com.quasar.fire.domain.port.in.LocateSpacecraftUseCase;
import com.quasar.fire.domain.port.out.SatelliteStationRepository;
import com.quasar.fire.domain.service.MessageReconstructionService;
import com.quasar.fire.domain.service.TrilaterationService;

import java.util.List;

public class LocateSpacecraftUseCaseImpl implements LocateSpacecraftUseCase {

    private final SatelliteStationRepository stationRepository;
    private final TrilaterationService trilaterationService;
    private final MessageReconstructionService messageService;

    public LocateSpacecraftUseCaseImpl(
            SatelliteStationRepository stationRepository,
            TrilaterationService trilaterationService,
            MessageReconstructionService messageService
    ) {
        this.stationRepository = stationRepository;
        this.trilaterationService = trilaterationService;
        this.messageService = messageService;
    }

    @Override
    public SpacecraftLocation locate(List<SatelliteName> names, List<Distance> distances, List<MessageFragment> messages) {
        List<SatelliteStation> stations = stationRepository.findByNames(names);
        Coordinates position = trilaterationService.locate(stations, distances);
        String reconstructedMessage = messageService.reconstruct(messages);
        return new SpacecraftLocation(position, reconstructedMessage);
    }
}
