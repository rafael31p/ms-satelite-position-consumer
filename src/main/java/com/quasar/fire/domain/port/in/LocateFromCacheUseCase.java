package com.quasar.fire.domain.port.in;

import com.quasar.fire.domain.model.SpacecraftLocation;

public interface LocateFromCacheUseCase {

    SpacecraftLocation locate();
}
