package com.quasar.fire.domain.service;

import com.quasar.fire.domain.exception.InsufficientSatellitesException;
import com.quasar.fire.domain.exception.NoUniqueIntersectionException;
import com.quasar.fire.domain.model.Coordinates;
import com.quasar.fire.domain.model.Distance;
import com.quasar.fire.domain.model.SatelliteStation;

import java.util.List;

public class TrilaterationService {

    private static final int REQUIRED_STATIONS = 3;
    private static final double EPSILON = 1e-6;

    public Coordinates locate(List<SatelliteStation> stations, List<Distance> distances) {
        if (stations.size() != REQUIRED_STATIONS || distances.size() != REQUIRED_STATIONS) {
            throw new InsufficientSatellitesException(Math.min(stations.size(), distances.size()));
        }

        Coordinates p1 = stations.get(0).position();
        Coordinates p2 = stations.get(1).position();
        Coordinates p3 = stations.get(2).position();

        double r1 = distances.get(0).value();
        double r2 = distances.get(1).value();
        double r3 = distances.get(2).value();

        double a = 2 * (p2.x() - p1.x());
        double b = 2 * (p2.y() - p1.y());
        double c = sq(r1) - sq(r2) - sq(p1.x()) + sq(p2.x()) - sq(p1.y()) + sq(p2.y());

        double d = 2 * (p3.x() - p2.x());
        double e = 2 * (p3.y() - p2.y());
        double f = sq(r2) - sq(r3) - sq(p2.x()) + sq(p3.x()) - sq(p2.y()) + sq(p3.y());

        double denominator = a * e - b * d;
        if (Math.abs(denominator) < EPSILON) {
            throw new NoUniqueIntersectionException();
        }

        double x = (c * e - b * f) / denominator;
        double y = (a * f - c * d) / denominator;

        return new Coordinates(x, y);
    }

    private double sq(double v) {
        return v * v;
    }
}
