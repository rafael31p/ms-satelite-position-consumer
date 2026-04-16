# Skill: Hexagonal Architecture con Java + Quarkus

## Descripcion
Guia para estructurar aplicaciones Quarkus siguiendo Arquitectura Hexagonal (Ports & Adapters). El objetivo es desacoplar el dominio de la infraestructura, permitiendo que la logica de negocio sea testeable e independiente del framework.

## Estructura de paquetes

```
com.quasar.fire/
  domain/                        # Nucleo del dominio (sin dependencias externas)
    model/                       # Entidades y Value Objects
    port/
      in/                        # Puertos de entrada (use cases)
      out/                       # Puertos de salida (repositorios, servicios externos)
    exception/                   # Excepciones de dominio
    service/                     # Implementacion de use cases (orquestadores)

  application/                   # Capa de aplicacion
    usecase/                     # Implementaciones de los puertos de entrada
                                 # Orquestan llamadas al dominio

  infrastructure/                # Adaptadores (dependen de frameworks)
    adapter/
      in/
        rest/                    # Controllers / Resources (JAX-RS)
        rest/dto/                # DTOs de entrada/salida (request/response)
        rest/mapper/             # Mappers DTO <-> Domain
      out/
        persistence/             # Implementaciones de puertos de salida
        persistence/entity/      # Entidades de cache/DB
        persistence/mapper/      # Mappers Entity <-> Domain
    config/                      # Configuracion de beans, health checks
```

## Reglas fundamentales

1. **El dominio NUNCA importa clases de infraestructura** (ni Quarkus, ni Jakarta, ni Infinispan, ni Jackson)
2. **Los puertos son interfaces Java puras** definidas en el dominio
3. **Los adaptadores implementan los puertos** y viven en infraestructura
4. **La inyeccion de dependencias** (CDI de Quarkus) conecta adaptadores con puertos en la capa de configuracion
5. **Los DTOs de REST no son las entidades de dominio** — siempre usar mappers
6. **Los Value Objects del dominio son inmutables** (records de Java 21)

## Flujo de una peticion

```
HTTP Request
  -> Adapter IN (Resource/Controller)
    -> Mapper (DTO -> Domain)
      -> Port IN (UseCase interface)
        -> Domain Service (logica pura)
          -> Port OUT (Repository interface)
            -> Adapter OUT (InfinispanRepository impl)
              -> Cache/DB
```

## Ejemplo de puerto de entrada

```java
// domain/port/in/CalculatePositionUseCase.java
public interface CalculatePositionUseCase {
    SpacecraftResponse calculate(List<SatelliteSignal> signals);
}
```

## Ejemplo de puerto de salida

```java
// domain/port/out/SatelliteRepository.java
public interface SatelliteRepository {
    Optional<Satellite> findByName(String name);
    List<Satellite> findAllByNames(List<String> names);
}
```

## Ejemplo de adaptador de salida

```java
// infrastructure/adapter/out/persistence/InfinispanSatelliteRepository.java
@ApplicationScoped
public class InfinispanSatelliteRepository implements SatelliteRepository {
    // Aqui si se permite usar @Remote, RemoteCache, ObjectMapper, etc.
}
```

## Aplicacion a Quarkus

- `@ApplicationScoped` solo en adaptadores y configuracion, NO en domain services
- Los domain services se registran como beans via `@Produces` en una clase de config
- Las anotaciones JAX-RS (`@Path`, `@POST`, etc.) solo en adaptadores REST
- `@ConfigProperty` solo en adaptadores o configuracion, nunca en dominio
- Los records de dominio NO llevan `@RegisterForReflection` — eso va en los DTOs de infra

## Cuando aplicar esta skill

- Al crear nuevos endpoints o funcionalidades
- Al refactorizar servicios existentes para desacoplarlos
- Al detectar que el dominio importa clases de framework
- Cuando los tests unitarios requieren levantar el contenedor Quarkus para probar logica de negocio
