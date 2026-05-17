# ms-satelite-position-consumer

Microservicio que calcula la posicion de una nave en el espacio mediante **trilateracion** a partir de las distancias reportadas por tres satelites, y reconstruye un mensaje fragmentado interceptado por cada satelite.

---

## Tecnologias

| Componente | Version | Descripcion |
|------------|---------|-------------|
| **Quarkus** | 3.33.1 LTS | Framework Java cloud-native con startup ultrarapido y bajo consumo de memoria |
| **OpenJDK** | 25 | Plataforma Java estandar (`maven.compiler.release=25`) |
| **GraalVM / Mandrel** | 25.0.x | Compilacion nativa para imagen container minima |
| **Infinispan** | 16.0.x | Cache distribuido en memoria (caches: `SATELLITE_POSITIONS`, `NAVE_POSITIONS`) |
| **Quarkus REST** | — | RESTful endpoints (sucesor de RESTEasy Reactive) |
| **SmallRye Fault Tolerance** | — | Retry, CircuitBreaker, Timeout para resiliencia |
| **SmallRye Health** | — | Liveness y Readiness probes para K8s |
| **Micrometer + Prometheus** | — | Metricas custom (`@Counted`, `@Timed`) |
| **SmallRye OpenAPI** | — | Documentacion automatica con Swagger UI |
| **Hibernate Validator** | — | Bean Validation (`@Valid`, `@NotBlank`, etc.) |
| **Docker / Podman** | — | Runtime de contenedores |

---

## Arquitectura

Sigue el patron **hexagonal (Ports & Adapters)** con **Domain-Driven Design**:

```
src/main/java/com/quasar/fire/
├── domain/                         # Logica pura, sin dependencias de framework
│   ├── model/                      # Value Objects y Entities
│   ├── service/                    # Domain Services (Trilateration, MessageReconstruction)
│   ├── exception/                  # Excepciones de dominio
│   └── port/
│       ├── in/                     # Interfaces de Use Cases
│       └── out/                    # Interfaces de Repositorios
│
├── application/
│   └── usecase/                    # Implementacion de Use Cases
│
└── infrastructure/
    ├── adapter/
    │   ├── in/rest/                # Endpoints REST (Quarkus REST)
    │   │   ├── dto/                # DTOs de request/response
    │   │   └── mapper/             # Mappers DTO <-> Domain
    │   └── out/persistence/        # Repositorios Infinispan
    │       └── entity/             # Entidades de cache (JSON)
    ├── config/                     # CDI bean wiring (BeanConfiguration)
    ├── exception/                  # Global exception mapper REST
    └── health/                     # Health checks K8s
```

Diagramas de componentes detallados: ver [`docs/architecture-proposal.md`](docs/architecture-proposal.md)

---

## Endpoints REST

Base path: `/quasar/fire/Api/v1`

| Metodo | Path | Descripcion |
|--------|------|-------------|
| `POST` | `/topsecret` | Calcula posicion + reconstruye mensaje desde 3 senales (1 sola request) |
| `POST` | `/topsecret_split/{satellite_name}` | Registra senal de un satelite individual en cache (TTL configurable) |
| `GET` | `/topsecret_split` | Calcula posicion desde senales en cache (requiere las 3 senales registradas) |
| `GET` | `/q/health/live` | Liveness probe |
| `GET` | `/q/health/ready` | Readiness probe (verifica conexion a Infinispan) |
| `GET` | `/q/metrics` | Metricas Prometheus |
| `GET` | `/quasar/fire/Api/v1/openapi` | Documento OpenAPI |
| `GET` | `/quasar/fire/Api/v1/swagger-ui` | Swagger UI (solo en `dev` y `test`) |

---

## Prerequisitos

| Herramienta | Como instalar |
|-------------|---------------|
| **JDK 25** | `winget install EclipseAdoptium.Temurin.25.JDK` (Windows) |
| **Maven 3.9+** | Incluido via `./mvnw` (wrapper) |
| **Podman** | `winget install RedHat.Podman` o usar Rancher Desktop |
| **Git** | `winget install Git.Git` |

Para compilacion **nativa** (opcional, para producir binario standalone):
- Mandrel JDK 25 (manejado automaticamente por `quarkus.native.container-build=true`)

---

## Compilacion y ejecucion local

### 1. Modo Dev (Quarkus DevServices auto-levanta Infinispan)

Asegurate de tener Podman corriendo:
```bash
podman machine start
export DOCKER_HOST="npipe:////./pipe/podman-machine-default"
```

Luego ejecuta:
```bash
./mvnw quarkus:dev
```

Esto levanta:
- App Quarkus en `http://localhost:8080`
- Infinispan en `http://localhost:11222` (admin / password)
- Swagger UI en `http://localhost:8080/quasar/fire/Api/v1/swagger-ui`

### 2. Cargar datos de satelites en cache

Abre `http://localhost:11222` con `admin/password`, crea cache `SATELLITE_POSITIONS` y agrega:

***key: kenobi***
```json
{ "name": "kenobi", "position": { "x": -500, "y": -200 } }
```
***key: skywalker***
```json
{ "name": "skywalker", "position": { "x": 100, "y": -100 } }
```
***key: sato***
```json
{ "name": "sato", "position": { "x": 500, "y": 100 } }
```

### 3. Ejemplos de invocacion

**POST `/topsecret`** — todas las senales en una sola request:
```bash
curl -X POST 'http://localhost:8080/quasar/fire/Api/v1/topsecret' \
  -H 'Content-Type: application/json' \
  -d '{
    "satellites": [
      {"name": "kenobi",    "distance": 447.213, "message": ["", "este", "es", "un", "mensaje"]},
      {"name": "skywalker", "distance": 223.606, "message": ["este", "", "un", "mensaje", "", "secreto"]},
      {"name": "sato",      "distance": 632.455, "message": ["", "", "es", "", "mensaje"]}
    ]
  }'
```

**POST `/topsecret_split/{name}`** — registrar senales individuales:
```bash
curl -X POST 'http://localhost:8080/quasar/fire/Api/v1/topsecret_split/kenobi' \
  -H 'Content-Type: application/json' \
  -d '{ "distance": 447.213, "message": ["", "este", "es", "un", "mensaje"] }'

curl -X POST 'http://localhost:8080/quasar/fire/Api/v1/topsecret_split/skywalker' \
  -H 'Content-Type: application/json' \
  -d '{ "distance": 223.606, "message": ["este", "", "un", "mensaje", "", "secreto"] }'

curl -X POST 'http://localhost:8080/quasar/fire/Api/v1/topsecret_split/sato' \
  -H 'Content-Type: application/json' \
  -d '{ "distance": 632.455, "message": ["", "", "es", "", "mensaje"] }'
```

**GET `/topsecret_split`** — calcular desde cache:
```bash
curl 'http://localhost:8080/quasar/fire/Api/v1/topsecret_split'
```

---

## Testing

### Ejecutar todos los tests (requiere Podman corriendo para tests de integracion)
```bash
./mvnw verify
```

### Solo tests unitarios de dominio (sin Podman)
```bash
./mvnw test -Dtest='*ServiceTest'
```

**Cobertura actual**: 13 tests
- 7 tests unitarios de dominio (`TrilaterationServiceTest`, `MessageReconstructionServiceTest`)
- 6 tests de integracion con DevServices Infinispan (`PositionResourceTest`, `SatellitePositionResourceTest`, `SatellitePositionResourceFailTest`)

---

## Empaquetado

### JAR estandar
```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Uber-JAR (todo en un solo JAR)
```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

### Imagen nativa (GraalVM via container)
```bash
./mvnw package -Pnative -Dquarkus.native.container-build=true
./target/ms-satelite-position-consumer-1.0.0-runner
```

Mas info: <https://quarkus.io/guides/maven-tooling>

---

## Configuracion por profile

| Profile | Activacion | Comportamiento |
|---------|-----------|----------------|
| `dev` | `./mvnw quarkus:dev` | DevServices levanta Infinispan automaticamente, Swagger UI activo, logs texto plano |
| `test` | Tests | DevServices automatico, ejecutado con `./mvnw test` |
| `prod` | `java -jar` o nativo | Usa variables de entorno, JSON logging, banner deshabilitado, Swagger UI deshabilitado |

### Variables de entorno (prod)

| Variable | Descripcion | Default |
|----------|-------------|---------|
| `INFINISPAN_SERVER_LIST` | Host:puerto de Infinispan | `infinispan:11222` |
| `INFINISPAN_USER` | Usuario Infinispan | (requerido) |
| `INFINISPAN_PASSWORD` | Password Infinispan | (requerido) |
| `CORS_ALLOWED_ORIGINS` | Origenes permitidos CORS | `http://localhost:3000` |

---

## Resiliencia

Los repositorios Infinispan estan protegidos con SmallRye Fault Tolerance:

```java
@Retry(maxRetries = 3, delay = 200, jitter = 100)
@Timeout(value = 10, unit = ChronoUnit.SECONDS)
@CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
```

- **Retry**: 3 reintentos con jitter aleatorio
- **Timeout**: aborta operaciones que pasen 10s
- **CircuitBreaker**: si 50% de 10 requests fallan, abre el circuito por 5s

---

## CI/CD

Pipeline en GitHub Actions ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)):

1. **Build & Test** con JDK 25 (Temurin)
2. **Compilacion nativa** con Mandrel (solo en push a `main`)
3. **Push** de imagen Docker a `ghcr.io/<owner>/ms-satelite-position-consumer`
4. **Deploy** a Azure VM via SSH (`docker pull` + `docker run`)

Secrets requeridos en el repo:
- `AZURE_VM` — IP/host de la VM
- `SSH_PRIVATE_KEY` — clave privada para SSH
- `INFINISPAN_USER`, `INFINISPAN_PASSWORD`

### Imagen Docker

`Dockerfile` usa imagen base `registry.access.redhat.com/ubi9/ubi-minimal:9.5` y copia el binario nativo (no requiere JVM en runtime).

---

## Despliegue en Kubernetes (local con K3s/Rancher Desktop)

Manifiestos disponibles en [`k8s/`](k8s/):

```bash
cd k8s && bash deploy.sh
```

Despliega:
- Namespace `quasar-fire`
- Infinispan (StatefulSet + PVC)
- PostgreSQL (StatefulSet + PVC)
- Kafka KRaft (StatefulSet + PVC)
- Microservicios consumer y tracker
- Stack Prometheus + Grafana
- Ingress Traefik

---

## Documentacion adicional

- [`docs/architecture-proposal.md`](docs/architecture-proposal.md) — Diagramas de componentes (Mermaid) y propuesta de microservicio adicional `ms-satellite-tracker`
- [`.claude/skills/`](.claude/skills/) — Skills de Claude Code: arquitectura hexagonal, SOLID, TDD, DDD

---

## Roadmap

Mejoras propuestas (ver `docs/architecture-proposal.md` para detalle):

- **Fase 1** (estabilidad): Ya aplicada — Bean Validation, Fault Tolerance, hexagonal refactor
- **Fase 2** (observabilidad): OpenTelemetry, distributed tracing, metricas custom
- **Fase 3** (seguridad): SmallRye JWT, CORS granular, rate limiting
- **Fase 4** (escalabilidad): Reactive end-to-end con `Uni<T>`, Kafka integration con `ms-satellite-tracker`, WebSocket `/locations/stream`

---

## Contacto

**Rafael Ignacio Pena Fiaga** — rafael31p@gmail.com
