# Skill: Guia de Refactoreo Integral

## Descripcion
Guia paso a paso para refactorizar ms-satelite-position-consumer aplicando las 4 skills de forma coordinada: Hexagonal Architecture, SOLID, Clean Code, TDD y DDD.

## Mapeo: Estado actual -> Estado objetivo

### Estructura de paquetes

```
ACTUAL:                                  OBJETIVO:
com.quasar.fire/                        com.quasar.fire/
  exceptions/custom/                      domain/
  exceptions/handlers/                      model/
  model/dtos/                                 Coordinates.java (record, VO)
  model/dtos/cache/                           Distance.java (record, VO)
  resources/                                  SatelliteName.java (record, VO)
  services/                                   MessageFragment.java (record, VO)
  services/impl/                              SatelliteStation.java (record, Entity)
  utils/                                      SatelliteSignal.java (record, Aggregate)
                                              SpacecraftLocation.java (record, VO)
                                            service/
                                              TrilaterationService.java (dominio puro)
                                              MessageReconstructionService.java
                                            port/in/
                                              LocateSpacecraftUseCase.java
                                              RegisterSignalUseCase.java
                                              LocateFromCacheUseCase.java
                                            port/out/
                                              SatelliteStationRepository.java
                                              SpacecraftSignalRepository.java
                                            exception/
                                              DomainException.java
                                              InsufficientSatellitesException.java
                                              NoUniqueIntersectionException.java
                                          application/
                                            usecase/
                                              LocateSpacecraftUseCaseImpl.java
                                              RegisterSignalUseCaseImpl.java
                                              LocateFromCacheUseCaseImpl.java
                                          infrastructure/
                                            adapter/in/rest/
                                              TopSecretResource.java
                                              TopSecretSplitResource.java
                                              dto/
                                                TrilaterationRequest.java
                                                TrilaterationResponse.java
                                                SatelliteSignalRequest.java
                                              mapper/
                                                SignalMapper.java
                                            adapter/out/persistence/
                                              InfinispanSatelliteStationRepository.java
                                              InfinispanSpacecraftSignalRepository.java
                                              entity/
                                                SatelliteStationEntity.java
                                                SpacecraftSignalEntity.java
                                              mapper/
                                                StationEntityMapper.java
                                                SignalEntityMapper.java
                                            config/
                                              BeanConfiguration.java
                                              InfinispanReadinessCheck.java
                                            exception/
                                              GlobalExceptionHandler.java
```

### Mapeo de clases actuales -> nuevas

| Clase actual                               | Destino                                         |
|--------------------------------------------|-------------------------------------------------|
| `CalculatePositionAndGenerateMessageUtil`  | `TrilaterationService` + `MessageReconstructionService` |
| `PositionCalculateServiceImpl`             | `LocateSpacecraftUseCaseImpl` + `RegisterSignalUseCaseImpl` |
| `ConsultSatelliteServiceImpl`              | `InfinispanSatelliteStationRepository`          |
| `ConsultNavePositionServiceImpl`           | `InfinispanSpacecraftSignalRepository`           |
| `SatellitesPositionResource`               | `TopSecretResource`                              |
| `PositionResource`                         | `TopSecretSplitResource`                         |
| `CustomRequest`                            | `TrilaterationRequest` (infra) + `TrilaterationCommand` (dominio) |
| `CustomResponse`                           | `TrilaterationResponse` (infra) + `SpacecraftLocation` (dominio) |
| `Position`                                 | `Coordinates` (dominio, Value Object)            |
| `SatelliteDistance`                        | `SatelliteSignal` (dominio, Aggregate)           |
| `Satellite` (cache)                        | `SatelliteStation` (dominio, Entity)             |
| `SatellitePositions` (cache)               | `SpacecraftSignalEntity` (infra)                 |
| `CustomFuntionalException`                 | `DomainException` + subclases                    |
| `CustomTechnicalException`                 | `InfrastructureException` en capa infra          |
| `CustomExceptionHandler`                   | `GlobalExceptionHandler` en infra                |
| `Constants`                                | Eliminar — usar constantes en la clase que las usa |

## Orden de refactoreo (TDD first)

### Fase 1: Dominio (sin tocar infra existente)
1. Crear Value Objects: `Coordinates`, `Distance`, `SatelliteName`, `MessageFragment`
2. Escribir tests unitarios para `TrilaterationService` (RED)
3. Implementar `TrilaterationService` con logica pura (GREEN)
4. Escribir tests unitarios para `MessageReconstructionService` (RED)
5. Implementar `MessageReconstructionService` (GREEN)
6. Crear excepciones de dominio
7. Crear interfaces de puertos (in y out)

### Fase 2: Application (use cases)
8. Escribir tests para `LocateSpacecraftUseCaseImpl` con mocks de puertos (RED)
9. Implementar use case (GREEN)
10. Repetir para `RegisterSignalUseCaseImpl` y `LocateFromCacheUseCaseImpl`

### Fase 3: Infrastructure (adaptadores)
11. Implementar `InfinispanSatelliteStationRepository` (adapta puerto out)
12. Implementar `InfinispanSpacecraftSignalRepository` (adapta puerto out)
13. Crear DTOs de REST y mappers
14. Refactorizar Resources para usar use cases
15. Mover exception handler

### Fase 4: Limpieza
16. Eliminar paquetes y clases legacy
17. Actualizar tests de integracion
18. Verificar cobertura con Jacoco

## Cuando aplicar esta skill

- Como roadmap principal para el refactoreo de la aplicacion
- Para verificar que cada cambio respeta las 4 skills
- Como checklist de progreso
