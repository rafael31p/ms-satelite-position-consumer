package com.quasar.fire.domain.port.in;

import com.quasar.fire.domain.model.Distance;
import com.quasar.fire.domain.model.MessageFragment;
import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SpacecraftLocation;

import java.util.List;

public interface LocateSpacecraftUseCase {

    SpacecraftLocation locate(List<SatelliteName> names, List<Distance> distances, List<MessageFragment> messages);
}
