package com.quasar.fire.application.usecase;

import com.quasar.fire.domain.exception.InsufficientSatellitesException;
import com.quasar.fire.domain.model.Coordinates;
import com.quasar.fire.domain.model.Distance;
import com.quasar.fire.domain.model.MessageFragment;
import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SatelliteSignal;
import com.quasar.fire.domain.model.SatelliteStation;
import com.quasar.fire.domain.model.SpacecraftLocation;
import com.quasar.fire.domain.port.in.LocateFromCacheUseCase;
import com.quasar.fire.domain.port.out.SatelliteStationRepository;
import com.quasar.fire.domain.port.out.SpacecraftSignalRepository;
import com.quasar.fire.domain.service.MessageReconstructionService;
import com.quasar.fire.domain.service.TrilaterationService;

import java.util.Comparator;
import java.util.List;

public class LocateFromCacheUseCaseImpl implements LocateFromCacheUseCase {

    private static final int REQUIRED_SIGNALS = 3;

    private final SpacecraftSignalRepository signalRepository;
    private final SatelliteStationRepository stationRepository;
    private final TrilaterationService trilaterationService;
    private final MessageReconstructionService messageService;
    private final List<SatelliteName> satelliteNames;

    public LocateFromCacheUseCaseImpl(
            SpacecraftSignalRepository signalRepository,
            SatelliteStationRepository stationRepository,
            TrilaterationService trilaterationService,
            MessageReconstructionService messageService,
            List<SatelliteName> satelliteNames
    ) {
        this.signalRepository = signalRepository;
        this.stationRepository = stationRepository;
        this.trilaterationService = trilaterationService;
        this.messageService = messageService;
        this.satelliteNames = satelliteNames;
    }

    @Override
    public SpacecraftLocation locate() {
        List<SatelliteSignal> signals = signalRepository.findByNames(satelliteNames);
        if (signals.size() < REQUIRED_SIGNALS) {
            throw new InsufficientSatellitesException(signals.size());
        }

        List<SatelliteSignal> sorted = signals.stream()
                .sorted(Comparator.comparing(SatelliteSignal::name))
                .toList();

        List<SatelliteName> sortedNames = sorted.stream().map(SatelliteSignal::name).toList();
        List<Distance> distances = sorted.stream().map(SatelliteSignal::distance).toList();
        List<MessageFragment> fragments = sorted.stream().map(SatelliteSignal::message).toList();

        List<SatelliteStation> stations = stationRepository.findByNames(sortedNames);

        Coordinates position = trilaterationService.locate(stations, distances);
        String message = messageService.reconstruct(fragments);

        return new SpacecraftLocation(position, message);
    }
}
