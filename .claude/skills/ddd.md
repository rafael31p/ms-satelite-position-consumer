# Skill: Domain-Driven Design (DDD) en Java 21 + Quarkus

## Descripcion
Guia para modelar el dominio de trilateracion satelital usando DDD tactico. El objetivo es que el codigo exprese el lenguaje del negocio y que las reglas vivan en el dominio, no en los servicios.

## Lenguaje Ubicuo (Ubiquitous Language)

Terminos del dominio que TODO el equipo usa consistentemente:

| Termino tecnico actual    | Termino DDD propuesto       | Significado                                    |
|---------------------------|-----------------------------|------------------------------------------------|
| `SatelliteDistance`       | `SatelliteSignal`           | Senal recibida: nombre, distancia, fragmento   |
| `Position`                | `Coordinates`               | Par (x, y) en el plano                         |
| `CustomRequest`           | `TrilaterationCommand`      | Comando para calcular posicion                 |
| `CustomResponse`          | `SpacecraftLocation`        | Resultado: coordenadas + mensaje reconstruido  |
| `calculatePosition`       | `locateSpacecraft`          | Accion de negocio principal                    |
| `getMessage`              | `reconstructMessage`        | Reconstruir mensaje de fragmentos              |
| `registerNavePosition`    | `registerSignal`            | Registrar senal individual de un satelite      |
| `Satellite` (cache)       | `SatelliteStation`          | Estacion satelital con posicion fija           |
| `NAVE_POSITIONS`          | `SpacecraftSignals`         | Senales registradas de la nave                 |
| `SATELLITE_POSITIONS`     | `SatelliteStations`         | Posiciones de referencia de los satelites      |

## Building Blocks DDD

### Entidades
Objetos con identidad unica que persiste en el tiempo:

```java
// El satelite tiene identidad (su nombre) y estado fijo
public record SatelliteStation(
    SatelliteName name,
    Coordinates position
) {
    // Identidad: name
    // Igualdad basada en identidad, no en atributos
}
```

### Value Objects
Objetos inmutables definidos por sus atributos, sin identidad:

```java
public record Coordinates(double x, double y) {
    public Coordinates {
        if (Double.isNaN(x) || Double.isNaN(y))
            throw new InvalidCoordinatesException(x, y);
    }

    public double distanceTo(Coordinates other) {
        return Math.sqrt(Math.pow(x - other.x(), 2) + Math.pow(y - other.y(), 2));
    }
}

public record Distance(double value) {
    public Distance {
        if (value < 0) throw new NegativeDistanceException(value);
    }
}

public record SatelliteName(String value) {
    public SatelliteName {
        if (value == null || value.isBlank())
            throw new InvalidSatelliteNameException(value);
    }
}

public record MessageFragment(String[] words) {
    public MessageFragment {
        Objects.requireNonNull(words, "words must not be null");
    }
}
```

### Agregados
Cluster de entidades y value objects con un raiz (Aggregate Root):

```java
// SatelliteSignal es un agregado: agrupa la senal recibida de un satelite
public record SatelliteSignal(
    SatelliteName name,
    Distance distance,
    MessageFragment message,
    LocalDateTime receivedAt
) {
    public boolean isExpired(long ttlSeconds) {
        return Duration.between(receivedAt, LocalDateTime.now()).getSeconds() > ttlSeconds;
    }
}
```

### Domain Services
Logica que no pertenece a una sola entidad o value object:

```java
// Servicio de dominio puro: sin framework, sin anotaciones, sin I/O
public class TrilaterationService {

    private static final int REQUIRED_STATIONS = 3;
    private static final double EPSILON = 1e-6;

    public Coordinates locate(List<SatelliteStation> stations, List<Distance> distances) {
        if (stations.size() != REQUIRED_STATIONS)
            throw new InsufficientSatellitesException(stations.size());

        // Algoritmo de Cramer...
        return new Coordinates(x, y);
    }
}

public class MessageReconstructionService {

    public String reconstruct(List<MessageFragment> fragments) {
        // Logica de reconstruccion...
    }
}
```

### Domain Events (opcional, para futuro)

```java
public record SpacecraftLocated(
    Coordinates position,
    String message,
    LocalDateTime occurredAt
) {}

public record SignalRegistered(
    SatelliteName satellite,
    LocalDateTime occurredAt
) {}
```

## Excepciones de Dominio

```java
// Jerarquia de excepciones del dominio
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
}

public class InsufficientSatellitesException extends DomainException {
    public InsufficientSatellitesException(int count) {
        super("Se requieren 3 satelites, se recibieron: " + count);
    }
}

public class NoUniqueIntersectionException extends DomainException {
    public NoUniqueIntersectionException() {
        super("Los circulos no tienen una interseccion unica");
    }
}

public class SignalExpiredException extends DomainException {
    public SignalExpiredException(String satellite) {
        super("La senal del satelite " + satellite + " ha expirado");
    }
}
```

## Puertos (Interfaces del dominio)

```java
// Puerto de salida: el dominio define QUE necesita, no COMO se obtiene
public interface SatelliteStationRepository {
    List<SatelliteStation> findByNames(List<SatelliteName> names);
}

public interface SpacecraftSignalRepository {
    Optional<SatelliteSignal> findByName(SatelliteName name);
    List<SatelliteSignal> findAll();
    void save(SatelliteSignal signal);
}
```

## Casos de Uso (Application Layer)

```java
// Orquestadores: coordinan dominio + puertos, sin logica de negocio
public class LocateSpacecraftUseCase {

    private final SatelliteStationRepository stationRepo;
    private final TrilaterationService trilaterationService;
    private final MessageReconstructionService messageService;

    public SpacecraftLocation execute(TrilaterationCommand command) {
        List<SatelliteStation> stations = stationRepo.findByNames(command.satelliteNames());
        Coordinates position = trilaterationService.locate(stations, command.distances());
        String message = messageService.reconstruct(command.messageFragments());
        return new SpacecraftLocation(position, message);
    }
}
```

## Anti-patrones a evitar

1. **Anemic Domain Model**: Entidades con solo getters, logica en servicios
   - El dominio DEBE tener comportamiento (`isExpired()`, `distanceTo()`, validacion en constructores)

2. **Smart UI**: Logica de negocio en Resources/Controllers
   - Los Resources solo mapean HTTP <-> Dominio

3. **Database-Driven Design**: Modelar basandose en tablas/caches
   - Modelar basandose en el NEGOCIO; el cache es un detalle de implementacion

4. **Primitive Obsession**: Usar `String` para nombre, `double` para distancia
   - Usar Value Objects: `SatelliteName`, `Distance`, `Coordinates`

## Cuando aplicar esta skill

- Al crear nuevas entidades o value objects
- Al definir reglas de negocio (validaciones, calculos)
- Al nombrar clases, metodos y variables
- Al decidir donde vive la logica (entidad vs servicio vs use case)
- Al modelar nuevos bounded contexts
