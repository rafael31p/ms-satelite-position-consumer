# Skill: Test-Driven Development (TDD) en Java 21 + Quarkus

## Descripcion
Guia para aplicar TDD en proyectos Quarkus. El ciclo Red-Green-Refactor guia el diseno del codigo, no solo su verificacion.

## Ciclo TDD

```
1. RED    - Escribir un test que falle (definir el comportamiento esperado)
2. GREEN  - Escribir el minimo codigo para que pase
3. REFACTOR - Limpiar sin romper tests
```

## Piramide de tests en Quarkus

```
         /  E2E  \          <- Pocos: REST completo con Infinispan real
        / Integracion \     <- Moderados: @QuarkusTest con DevServices
       /   Unitarios    \   <- Muchos: logica pura, sin framework
```

## Tests unitarios (dominio puro)

El dominio NO depende de Quarkus, entonces los tests son JUnit 5 puro:

```java
// Sin @QuarkusTest, sin contenedor, milisegundos de ejecucion
class TrilaterationServiceTest {

    private final TrilaterationService service = new TrilaterationService();

    @Test
    void shouldCalculatePositionFromThreeSatellites() {
        // Given
        var positions = List.of(
            new Position(-500, -200),
            new Position(100, -100),
            new Position(500, 100)
        );
        var distances = List.of(447.213, 223.606, 632.455);

        // When
        Position result = service.calculate(positions, distances);

        // Then
        assertThat(result.x()).isCloseTo(-81.25, within(0.01));
        assertThat(result.y()).isCloseTo(-112.50, within(0.01));
    }

    @Test
    void shouldThrowWhenCirclesDontIntersect() {
        var positions = List.of(
            new Position(0, 0),
            new Position(0, 0),
            new Position(0, 0)
        );
        var distances = List.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> service.calculate(positions, distances))
            .isInstanceOf(NoUniqueIntersectionException.class);
    }
}
```

## Tests de integracion (adaptadores)

Usan `@QuarkusTest` + DevServices para validar que los adaptadores funcionan con infraestructura real:

```java
@QuarkusTest
class InfinispanSatelliteRepositoryTest {

    @Inject
    SatelliteRepository repository;  // el puerto, no la implementacion

    @Test
    void shouldFindSatelliteByName() {
        // Given: datos precargados en @BeforeEach

        // When
        Optional<Satellite> result = repository.findByName("kenobi");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().position()).isEqualTo(new Position(-500, -200));
    }
}
```

## Tests de API (end-to-end)

```java
@QuarkusTest
class PositionResourceIT {

    @Test
    void shouldReturn200WithPositionAndMessage() {
        given()
            .contentType(ContentType.JSON)
            .body(validRequest())
        .when()
            .post("/topsecret")
        .then()
            .statusCode(200)
            .body("position.x", closeTo(-81.25, 0.01))
            .body("message", equalTo("este es un mensaje secreto"));
    }

    @Test
    void shouldReturn404WhenSatelliteMissing() {
        given()
            .contentType(ContentType.JSON)
            .body(incompleteRequest())
        .when()
            .post("/topsecret")
        .then()
            .statusCode(404);
    }
}
```

## Patron Given-When-Then (Arrange-Act-Assert)

Cada test tiene exactamente 3 secciones:
```java
@Test
void shouldReconstructMessageFromFragments() {
    // Given (Arrange)
    var fragments = List.of(
        new String[]{"", "este", "es", "un", "mensaje"},
        new String[]{"este", "", "un", "mensaje", "", "secreto"},
        new String[]{"", "", "es", "", "mensaje"}
    );

    // When (Act)
    String message = service.reconstruct(fragments);

    // Then (Assert)
    assertThat(message).isEqualTo("este es un mensaje secreto");
}
```

## Reglas de tests

1. **Un assert logico por test** (puede ser multiples asserts del mismo concepto)
2. **Tests independientes** — no dependen del orden de ejecucion
3. **Nombres descriptivos**: `should[Behavior]When[Condition]`
4. **No testear implementacion** — testear comportamiento observable
5. **Tests rapidos**: unitarios < 100ms, integracion < 5s
6. **No mockear lo que no te pertenece** — mockear puertos, no librerias externas

## Test doubles

```java
// En hexagonal, mockear los PUERTOS DE SALIDA para tests de dominio:
class CalculatePositionUseCaseTest {

    // Mock del puerto de salida
    SatelliteRepository satelliteRepo = mock(SatelliteRepository.class);
    SpacecraftSignalRepository signalRepo = mock(SpacecraftSignalRepository.class);

    // SUT: el servicio de dominio con mocks inyectados
    CalculatePositionService service = new CalculatePositionService(
        satelliteRepo, signalRepo, new TrilaterationService()
    );

    @Test
    void shouldCalculatePositionFromCache() {
        // Given
        when(satelliteRepo.findAllByNames(any())).thenReturn(satellites());
        when(signalRepo.findAll()).thenReturn(signals());

        // When
        SpacecraftPosition result = service.calculateFromCache();

        // Then
        assertThat(result.position().x()).isCloseTo(-81.25, within(0.01));
        verify(satelliteRepo).findAllByNames(any());
    }
}
```

## Cobertura

- Objetivo minimo: 80% en dominio, 70% en adaptadores
- Jacoco ya esta configurado en el proyecto (`quarkus-jacoco`)
- Covertura NO es calidad — un test sin asserts tiene 100% de cobertura y 0% de valor

## Cuando aplicar esta skill

- ANTES de escribir cualquier codigo nuevo (Red first)
- Al corregir un bug: primero el test que reproduce el bug, despues el fix
- Al refactorizar: asegurar que los tests existentes siguen pasando
- Al agregar un nuevo endpoint o caso de uso
