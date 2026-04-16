package com.quasar.fire.infrastructure.config;

import com.quasar.fire.application.usecase.LocateFromCacheUseCaseImpl;
import com.quasar.fire.application.usecase.LocateSpacecraftUseCaseImpl;
import com.quasar.fire.application.usecase.RegisterSignalUseCaseImpl;
import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.port.in.LocateFromCacheUseCase;
import com.quasar.fire.domain.port.in.LocateSpacecraftUseCase;
import com.quasar.fire.domain.port.in.RegisterSignalUseCase;
import com.quasar.fire.domain.port.out.SatelliteStationRepository;
import com.quasar.fire.domain.port.out.SpacecraftSignalRepository;
import com.quasar.fire.domain.service.MessageReconstructionService;
import com.quasar.fire.domain.service.TrilaterationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class BeanConfiguration {

    @ConfigProperty(name = "quasar.fire.constants.name.satellites")
    String satelliteNames;

    @Produces
    @ApplicationScoped
    public TrilaterationService trilaterationService() {
        return new TrilaterationService();
    }

    @Produces
    @ApplicationScoped
    public MessageReconstructionService messageReconstructionService() {
        return new MessageReconstructionService();
    }

    @Produces
    @ApplicationScoped
    public LocateSpacecraftUseCase locateSpacecraftUseCase(
            SatelliteStationRepository stationRepo,
            TrilaterationService trilaterationService,
            MessageReconstructionService messageService
    ) {
        return new LocateSpacecraftUseCaseImpl(stationRepo, trilaterationService, messageService);
    }

    @Produces
    @ApplicationScoped
    public RegisterSignalUseCase registerSignalUseCase(SpacecraftSignalRepository signalRepo) {
        return new RegisterSignalUseCaseImpl(signalRepo);
    }

    @Produces
    @ApplicationScoped
    public LocateFromCacheUseCase locateFromCacheUseCase(
            SpacecraftSignalRepository signalRepo,
            SatelliteStationRepository stationRepo,
            TrilaterationService trilaterationService,
            MessageReconstructionService messageService
    ) {
        List<SatelliteName> names = Arrays.stream(satelliteNames.split(","))
                .map(SatelliteName::new)
                .toList();
        return new LocateFromCacheUseCaseImpl(signalRepo, stationRepo, trilaterationService, messageService, names);
    }
}
