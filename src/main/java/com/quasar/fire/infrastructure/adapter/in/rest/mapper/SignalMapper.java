package com.quasar.fire.infrastructure.adapter.in.rest.mapper;

import com.quasar.fire.domain.model.Coordinates;
import com.quasar.fire.domain.model.Distance;
import com.quasar.fire.domain.model.MessageFragment;
import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SatelliteSignal;
import com.quasar.fire.domain.model.SpacecraftLocation;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.SatelliteSignalRequest;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.TrilaterationRequest;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.TrilaterationResponse;

import java.time.LocalDateTime;
import java.util.List;

public class SignalMapper {

    private SignalMapper() {
    }

    public static List<SatelliteName> toNames(TrilaterationRequest request) {
        return request.satellites().stream()
                .map(s -> new SatelliteName(s.name()))
                .toList();
    }

    public static List<Distance> toDistances(TrilaterationRequest request) {
        return request.satellites().stream()
                .map(s -> new Distance(s.distance()))
                .toList();
    }

    public static List<MessageFragment> toFragments(TrilaterationRequest request) {
        return request.satellites().stream()
                .map(s -> new MessageFragment(s.message()))
                .toList();
    }

    public static SatelliteSignal toSignal(SatelliteSignalRequest request, String satelliteName) {
        return new SatelliteSignal(
                new SatelliteName(satelliteName),
                new Distance(request.distance()),
                new MessageFragment(request.message()),
                LocalDateTime.now()
        );
    }

    public static TrilaterationResponse toResponse(SpacecraftLocation location) {
        Coordinates pos = location.position();
        return new TrilaterationResponse(
                new TrilaterationResponse.PositionDto(pos.x(), pos.y()),
                location.message()
        );
    }

    public static SatelliteSignalRequest toSignalResponse(SatelliteSignal signal) {
        return new SatelliteSignalRequest(
                signal.name().value(),
                signal.distance().value(),
                signal.message().words()
        );
    }
}
