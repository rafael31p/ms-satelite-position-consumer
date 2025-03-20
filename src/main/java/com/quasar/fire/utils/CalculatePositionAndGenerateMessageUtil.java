package com.quasar.fire.utils;

import com.quasar.fire.exceptions.custom.CustomTechnicalException;
import com.quasar.fire.model.dtos.Position;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CalculatePositionAndGenerateMessageUtil {
    private static Position calculatePosition(List<Position> positions, List<Double> distances) {
        // Get satellite positions
        Position p1 = positions.get(0);  // Kenobi position (x1,y1)
        Position p2 = positions.get(1);  // Skywalker position (x2,y2)
        Position p3 = positions.get(2);  // Sato position (x3,y3)

        // Get distances from each satellite to the object
        double r1 = distances.get(0);    // Distance from Kenobi
        double r2 = distances.get(1);    // Distance from Skywalker
        double r3 = distances.get(2);    // Distance from Sato

        // Calculate coefficients for the system of equations
        // Using the formula: (x - xi)² + (y - yi)² = ri² for each satellite
        double A = 2 * (p2.x() - p1.x());
        double B = 2 * (p2.y() - p1.y());
        double C = Math.pow(r1, 2) - Math.pow(r2, 2) - Math.pow(p1.x(), 2) + Math.pow(p2.x(), 2) - Math.pow(p1.y(), 2) + Math.pow(p2.y(), 2);

        double D = 2 * (p3.x() - p2.x());
        double E = 2 * (p3.y() - p2.y());
        double F = Math.pow(r2, 2) - Math.pow(r3, 2) - Math.pow(p2.x(), 2) + Math.pow(p3.x(), 2) - Math.pow(p2.y(), 2) + Math.pow(p3.y(), 2);

        // Calculate intersection point using Cramer's rule
        double denominator = (A * E - B * D);
        if (Math.abs(denominator) < 0.000001) {
            throw new CustomTechnicalException("No existe intersección única entre los tres círculos");
        }

        // Calculate final position
        double x = (C * E - B * F) / denominator;
        double y = (A * F - C * D) / denominator;

        return new Position(x, y);
    }
    public static Position calculatePositionByOrder(List<Position> positions, List<Double> distances){
        // Actualmente posiciones: Kenobi, Sato, Skywalker (orden alfabético)
        // Se debes reordenar a: Kenobi, Skywalker, Sato (orden trilateración correcto)

        Position kenobi = positions.get(0);
        Position sato = positions.get(1);
        Position skywalker = positions.get(2);

        double dKenobi = distances.get(0);
        double dSato = distances.get(1);
        double dSkywalker = distances.get(2);

        // Reordeno claramente posiciones y distancias:
        List<Position> orderedPositions = List.of(kenobi, skywalker, sato);
        List<Double> orderedDistances = List.of(dKenobi, dSkywalker, dSato);

        // Ahora llamo al método trilateración claramente corregido
        return calculatePosition(orderedPositions, orderedDistances);
    }
    public static String getMessage(List<String[]> messages) {
        return IntStream.range(0, messages.stream().mapToInt(m -> m.length).max().orElse(0))
                .mapToObj(i -> messages.stream()
                        .filter(msg -> i < msg.length && !msg[i].isBlank())
                        .map(msg -> msg[i])
                        .findFirst().orElse(""))
                .filter(word -> !word.isBlank())
                .distinct()
                .collect(Collectors.joining(" "));
    }
}
