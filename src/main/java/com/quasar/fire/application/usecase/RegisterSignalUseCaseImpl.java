package com.quasar.fire.application.usecase;

import com.quasar.fire.domain.model.SatelliteSignal;
import com.quasar.fire.domain.port.in.RegisterSignalUseCase;
import com.quasar.fire.domain.port.out.SpacecraftSignalRepository;

import java.util.Optional;

public class RegisterSignalUseCaseImpl implements RegisterSignalUseCase {

    private final SpacecraftSignalRepository signalRepository;

    public RegisterSignalUseCaseImpl(SpacecraftSignalRepository signalRepository) {
        this.signalRepository = signalRepository;
    }

    @Override
    public SatelliteSignal register(SatelliteSignal signal, long ttlSeconds) {
        Optional<SatelliteSignal> existing = signalRepository.findByName(signal.name());

        if (existing.isPresent() && !existing.get().isExpired(ttlSeconds)) {
            return existing.get();
        }

        return signalRepository.save(signal);
    }
}
