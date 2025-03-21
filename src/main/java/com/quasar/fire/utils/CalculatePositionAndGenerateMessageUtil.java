package com.quasar.fire.utils;

import com.quasar.fire.exceptions.custom.CustomTechnicalException;
import com.quasar.fire.model.dtos.Position;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CalculatePositionAndGenerateMessageUtil {
    private static Position trilateration(List<Position> positions, List<Double> distances) {
        Position p1 = positions.get(0);  // Kenobi posicion (x1,y1)
        Position p2 = positions.get(1);  // Skywalker posicion (x2,y2)
        Position p3 = positions.get(2);  // Sato posicion (x3,y3)

        double r1 = distances.get(0);    // Distancia de Kenobi
        double r2 = distances.get(1);    // Distancia de Skywalker
        double r3 = distances.get(2);    // Distancia de Sato

        // Usando la formula: (x - xi)² + (y - yi)² = ri²
        double a = 2 * (p2.x() - p1.x());
        double b = 2 * (p2.y() - p1.y());
        double c = Math.pow(r1, 2) - Math.pow(r2, 2) - Math.pow(p1.x(), 2) + Math.pow(p2.x(), 2) - Math.pow(p1.y(), 2) + Math.pow(p2.y(), 2);

        double d = 2 * (p3.x() - p2.x());
        double e = 2 * (p3.y() - p2.y());
        double f = Math.pow(r2, 2) - Math.pow(r3, 2) - Math.pow(p2.x(), 2) + Math.pow(p3.x(), 2) - Math.pow(p2.y(), 2) + Math.pow(p3.y(), 2);

        // Se calcula la interseccion del punto usando la regla de Crammer
        double denominator = (a * e - b * d);
        if (Math.abs(denominator) < 0.000001) {
            throw new CustomTechnicalException("No existe intersección única entre los tres círculos");
        }

        // Calcula la posicion de la nave
        double x = (c * e - b * f) / denominator;
        double y = (a * f - c * d) / denominator;

        return new Position(x, y);
    }
    public static Position calculatePositionByOrder(List<Position> positions, List<Double> distances){
        // Actualmente posiciones: Kenobi, Sato, Skywalker (orden alfabético)
        Position kenobi = positions.get(0);
        Position sato = positions.get(1);
        Position skywalker = positions.get(2);

        double dKenobi = distances.get(0);
        double dSato = distances.get(1);
        double dSkywalker = distances.get(2);

        // Reordeno claramente posiciones y distancias:
        List<Position> orderedPositions = List.of(kenobi, skywalker, sato);
        List<Double> orderedDistances = List.of(dKenobi, dSkywalker, dSato);

        // Ahora llamo al metodo trilateración claramente corregido
        return trilateration(orderedPositions, orderedDistances);
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
    private CalculatePositionAndGenerateMessageUtil() {

    }
}
