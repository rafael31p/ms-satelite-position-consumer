package com.quasar.fire.domain.service;

import com.quasar.fire.domain.exception.InsufficientSatellitesException;
import com.quasar.fire.domain.exception.NoUniqueIntersectionException;
import com.quasar.fire.domain.model.Coordinates;
import com.quasar.fire.domain.model.Distance;
import com.quasar.fire.domain.model.SatelliteName;
import com.quasar.fire.domain.model.SatelliteStation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrilaterationServiceTest {

    private final TrilaterationService service = new TrilaterationService();

    @Test
    void shouldCalculatePositionFromThreeSatellites() {
        List<SatelliteStation> stations = List.of(
                new SatelliteStation(new SatelliteName("kenobi"), new Coordinates(-500, -200)),
                new SatelliteStation(new SatelliteName("sato"), new Coordinates(500, 100)),
                new SatelliteStation(new SatelliteName("skywalker"), new Coordinates(100, -100))
        );
        List<Distance> distances = List.of(
                new Distance(447.213),
                new Distance(632.455),
                new Distance(223.606)
        );

        Coordinates result = service.locate(stations, distances);

        assertEquals(-81.25, result.x(), 0.1);
        assertEquals(-112.50, result.y(), 0.1);
    }

    @Test
    void shouldThrowWhenLessThanThreeSatellites() {
        List<SatelliteStation> stations = List.of(
                new SatelliteStation(new SatelliteName("kenobi"), new Coordinates(-500, -200))
        );
        List<Distance> distances = List.of(new Distance(447.213));

        assertThrows(InsufficientSatellitesException.class,
                () -> service.locate(stations, distances));
    }

    @Test
    void shouldThrowWhenCirclesDontIntersect() {
        List<SatelliteStation> stations = List.of(
                new SatelliteStation(new SatelliteName("a"), new Coordinates(0, 0)),
                new SatelliteStation(new SatelliteName("b"), new Coordinates(0, 0)),
                new SatelliteStation(new SatelliteName("c"), new Coordinates(0, 0))
        );
        List<Distance> distances = List.of(
                new Distance(1.0), new Distance(2.0), new Distance(3.0)
        );

        assertThrows(NoUniqueIntersectionException.class,
                () -> service.locate(stations, distances));
    }
}
