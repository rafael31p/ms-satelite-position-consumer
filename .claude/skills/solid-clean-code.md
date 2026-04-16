# Skill: Principios SOLID y Clean Code en Java 21 + Quarkus

## Descripcion
Guia para escribir codigo limpio, mantenible y extensible aplicando SOLID y las practicas de Clean Code de Robert C. Martin, adaptadas al ecosistema Java 21 con Quarkus.

## S - Single Responsibility Principle (SRP)

Cada clase tiene UNA sola razon para cambiar.

**Violacion tipica:**
```java
// Un servicio que calcula posicion, reconstruye mensajes, valida datos y accede al cache
public class PositionCalculateServiceImpl {
    public Uni<CustomResponse> calculatePosition(...)     // calcula posicion
    public Uni<SatelliteDistance> registerNavePosition(...) // gestiona cache
    public Uni<CustomResponse> calculatePositionByCache()  // orquesta desde cache
}
```

**Aplicacion correcta:**
```java
// Cada responsabilidad en su propia clase
public class TrilaterationService {           // solo calcula posicion
    Position calculate(List<Position> p, List<Double> d);
}
public class MessageReconstructionService {    // solo reconstruye mensaje
    String reconstruct(List<String[]> fragments);
}
public class SpacecraftRegistrationService {   // solo gestiona registro
    SatelliteSignal register(SatelliteSignal signal);
}
```

## O - Open/Closed Principle (OCP)

Abierto para extension, cerrado para modificacion.

- Usar interfaces (puertos) para definir contratos
- Nuevas implementaciones extienden comportamiento sin modificar existentes
- En Quarkus: `@Alternative` o `@Priority` para sustituir implementaciones

## L - Liskov Substitution Principle (LSP)

Las subclases deben ser sustituibles por sus clases base.

- Los records que implementan interfaces deben cumplir todos los contratos
- Las excepciones custom deben respetar la jerarquia de excepciones Java

## I - Interface Segregation Principle (ISP)

Interfaces pequenas y especificas, no interfaces gordas.

**Violacion:**
```java
public interface IPositionCalculateService {
    Uni<CustomResponse> calculatePosition(CustomRequest request);
    Uni<SatelliteDistance> registerNavePosition(SatelliteDistance sd, String name);
    Uni<CustomResponse> calculatePositionByCache();
}
```

**Correcto:**
```java
public interface CalculatePositionUseCase {
    Uni<CustomResponse> calculate(CustomRequest request);
}
public interface RegisterSignalUseCase {
    Uni<SatelliteDistance> register(SatelliteDistance sd, String name);
}
public interface CalculatePositionFromCacheUseCase {
    Uni<CustomResponse> calculate();
}
```

## D - Dependency Inversion Principle (DIP)

Depender de abstracciones, no de implementaciones.

- Los servicios de dominio dependen de interfaces (puertos), no de `RemoteCache`
- La inyeccion de dependencias conecta todo en runtime via CDI
- Constructor injection siempre (no field injection)

## Clean Code - Naming

```java
// Mal: nombres genericos o abreviados
CustomRequest, CustomResponse, SatelliteDistance, result, value
IPositionCalculateService  // prefijo "I" es convencion C#, no Java

// Bien: nombres que revelan intencion
CalculatePositionCommand, SpacecraftPositionResult, SatelliteSignal
PositionCalculator  // sin prefijo "I"
```

## Clean Code - Metodos

1. **Maximo 20 lineas** por metodo
2. **Un nivel de abstraccion** por metodo
3. **Sin efectos secundarios ocultos** — un metodo llamado `get` no debe escribir en cache
4. **Parametros**: maximo 3. Si necesitas mas, crear un Value Object
5. **Sin flags como parametros** — crear dos metodos distintos

## Clean Code - Manejo de errores

```java
// Mal: catch generico, excepciones como flujo de control
try { ... } catch (Exception e) { throw new CustomTechnicalException(...); }

// Bien: excepciones especificas, fail fast
public Position trilaterate(List<Position> positions, List<Double> distances) {
    if (positions.size() != 3) throw new InsufficientSatellitesException(positions.size());
    // ... logica limpia sin try/catch
}
```

## Clean Code - Records y Value Objects (Java 21)

```java
// Value Object inmutable, auto-validante
public record Position(double x, double y) {
    public Position {
        if (Double.isNaN(x) || Double.isNaN(y))
            throw new InvalidPositionException(x, y);
    }
}

public record SatelliteSignal(
    SatelliteName name,
    Distance distance,
    MessageFragment message
) {}
```

## Clean Code - Constantes

```java
// Mal: magic numbers y strings
if (positions.size() < 3) ...
if (Math.abs(denominator) < 0.000001) ...
messageErrorFuntional.replace("value", ...)

// Bien: constantes con nombre semantico
private static final int REQUIRED_SATELLITES = 3;
private static final double EPSILON = 1e-6;
```

## Cuando aplicar esta skill

- En cada nueva clase o metodo
- Al refactorizar codigo existente
- En code reviews
- Cuando un metodo supera las 20 lineas
- Cuando una clase tiene mas de una responsabilidad
- Cuando los nombres no son autoexplicativos
