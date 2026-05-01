# Propuesta de Arquitectura - Quasar Fire Platform

## 1. Diagrama de Componentes Actual (ms-satelite-position-consumer)

```mermaid
graph TB
    subgraph "Cliente / Operador"
        CLIENT[Cliente HTTP]
    end

    subgraph "ms-satelite-position-consumer"
        subgraph "Infrastructure Layer - Adapters IN"
            TSR[TopSecretResource<br/>POST /topsecret]
            TSSR[TopSecretSplitResource<br/>POST /topsecret_split/name<br/>GET /topsecret_split]
            GEH[GlobalExceptionHandler]
            SM[SignalMapper]
        end

        subgraph "Application Layer - Use Cases"
            LSU[LocateSpacecraftUseCaseImpl]
            RSU[RegisterSignalUseCaseImpl]
            LCU[LocateFromCacheUseCaseImpl]
        end

        subgraph "Domain Layer"
            subgraph "Services"
                TS[TrilaterationService]
                MRS[MessageReconstructionService]
            end
            subgraph "Ports IN"
                P_LS[LocateSpacecraftUseCase]
                P_RS[RegisterSignalUseCase]
                P_LC[LocateFromCacheUseCase]
            end
            subgraph "Ports OUT"
                P_SSR[SatelliteStationRepository]
                P_SPR[SpacecraftSignalRepository]
            end
            subgraph "Model"
                COORD[Coordinates]
                DIST[Distance]
                SNAME[SatelliteName]
                MSGF[MessageFragment]
                SSIG[SatelliteSignal]
                SSTA[SatelliteStation]
                SLOC[SpacecraftLocation]
            end
        end

        subgraph "Infrastructure Layer - Adapters OUT"
            ISSR[InfinispanSatelliteStationRepository]
            ISPR[InfinispanSpacecraftSignalRepository]
        end

        subgraph "Infrastructure - Cross-Cutting"
            HC_L[MyLivenessCheck]
            HC_R[InfinispanReadinessCheck]
            BEAN[BeanConfiguration]
        end
    end

    subgraph "External Systems"
        INFINISPAN[(Infinispan<br/>SATELLITE_POSITIONS<br/>NAVE_POSITIONS)]
    end

    CLIENT -->|HTTP| TSR
    CLIENT -->|HTTP| TSSR
    TSR --> SM --> P_LS
    TSSR --> SM --> P_RS
    TSSR --> P_LC
    P_LS --> LSU --> TS & MRS
    P_RS --> RSU
    P_LC --> LCU --> TS & MRS
    LSU --> P_SSR
    LCU --> P_SSR & P_SPR
    RSU --> P_SPR
    P_SSR --> ISSR --> INFINISPAN
    P_SPR --> ISPR --> INFINISPAN
    HC_R --> INFINISPAN
```

---

## 2. Diagrama de Componentes Mejorado (ms-satelite-position-consumer v2)

```mermaid
graph TB
    subgraph "Clientes"
        CLIENT[Cliente HTTP]
        WS_CLIENT[Cliente WebSocket]
        TRACKER[ms-satellite-tracker<br/>Microservicio]
    end

    subgraph "API Gateway / Security"
        GW[API Gateway<br/>Rate Limiting<br/>JWT Validation]
    end

    subgraph "ms-satelite-position-consumer v2"
        subgraph "Infrastructure Layer - Adapters IN"
            TSR[TopSecretResource<br/>POST /topsecret]
            TSSR[TopSecretSplitResource<br/>POST /topsecret_split/name<br/>GET /topsecret_split]
            SAT_R[SatelliteResource<br/>GET /satellites<br/>GET /satellites/name]
            HIST_R[LocationHistoryResource<br/>GET /locations/history]
            KAFKA_C[KafkaSignalConsumer<br/>Canal: satellite-signals]
            WS_EP[WebSocket /locations/stream]
            GEH[GlobalExceptionHandler]
            SM[SignalMapper]
        end

        subgraph "Application Layer - Use Cases"
            LSU[LocateSpacecraftUseCaseImpl]
            RSU[RegisterSignalUseCaseImpl]
            LCU[LocateFromCacheUseCaseImpl]
            QSU[QuerySatellitesUseCaseImpl<br/>NUEVO]
            HSU[LocationHistoryUseCaseImpl<br/>NUEVO]
        end

        subgraph "Domain Layer"
            subgraph "Services"
                TS[TrilaterationService]
                MRS[MessageReconstructionService]
            end
            subgraph "Ports IN"
                P_LS[LocateSpacecraftUseCase]
                P_RS[RegisterSignalUseCase]
                P_LC[LocateFromCacheUseCase]
                P_QS[QuerySatellitesUseCase<br/>NUEVO]
                P_HS[LocationHistoryUseCase<br/>NUEVO]
            end
            subgraph "Ports OUT"
                P_SSR[SatelliteStationRepository]
                P_SPR[SpacecraftSignalRepository]
                P_LHR[LocationHistoryRepository<br/>NUEVO]
                P_EVT[LocationEventPublisher<br/>NUEVO]
            end
            subgraph "Model"
                COORD[Coordinates]
                DIST[Distance]
                SNAME[SatelliteName]
                MSGF[MessageFragment]
                SSIG[SatelliteSignal]
                SSTA[SatelliteStation]
                SLOC[SpacecraftLocation]
                LHIST[LocationRecord<br/>NUEVO]
            end
        end

        subgraph "Infrastructure Layer - Adapters OUT"
            ISSR[InfinispanSatelliteStationRepository<br/>+ Cache L1 Caffeine]
            ISPR[InfinispanSpacecraftSignalRepository<br/>+ @Retry + @CircuitBreaker]
            ILHR[InfinispanLocationHistoryRepository<br/>NUEVO]
            KAFKA_P[KafkaLocationEventPublisher<br/>NUEVO]
        end

        subgraph "Infrastructure - Cross-Cutting"
            HC_L[LivenessCheck<br/>Mejorado]
            HC_R[ReadinessCheck<br/>Valida satelites]
            HC_S[StartupCheck<br/>NUEVO]
            BEAN[BeanConfiguration]
            OTEL[OpenTelemetry<br/>Tracing]
            FT[Fault Tolerance<br/>Retry + CircuitBreaker]
            SEC[SecurityFilter<br/>JWT / API Key]
            METRICS[Custom Metrics<br/>Micrometer]
            BOOT[SatelliteBootstrap<br/>@Startup NUEVO]
        end
    end

    subgraph "External Systems"
        INFINISPAN[(Infinispan<br/>SATELLITE_POSITIONS<br/>NAVE_POSITIONS<br/>LOCATION_HISTORY)]
        KAFKA[[Apache Kafka<br/>satellite-signals<br/>location-events]]
        JAEGER[Jaeger / Zipkin<br/>Tracing]
        PROMETHEUS[Prometheus<br/>Metrics]
        GRAFANA[Grafana<br/>Dashboards]
    end

    CLIENT -->|HTTPS| GW
    WS_CLIENT -->|WSS| GW
    TRACKER -->|Kafka| KAFKA

    GW -->|JWT validated| TSR & TSSR & SAT_R & HIST_R
    GW -->|WSS| WS_EP
    KAFKA --> KAFKA_C

    TSR --> SM --> P_LS
    TSSR --> SM --> P_RS & P_LC
    SAT_R --> P_QS
    HIST_R --> P_HS
    KAFKA_C --> SM --> P_RS

    P_LS --> LSU --> TS & MRS
    P_RS --> RSU
    P_LC --> LCU --> TS & MRS
    P_QS --> QSU
    P_HS --> HSU

    LSU --> P_SSR & P_EVT
    LCU --> P_SSR & P_SPR & P_EVT
    RSU --> P_SPR
    QSU --> P_SSR
    HSU --> P_LHR

    P_SSR --> ISSR --> INFINISPAN
    P_SPR --> ISPR --> INFINISPAN
    P_LHR --> ILHR --> INFINISPAN
    P_EVT --> KAFKA_P --> KAFKA
    KAFKA_P --> WS_EP

    OTEL --> JAEGER
    METRICS --> PROMETHEUS --> GRAFANA
    BOOT -->|@Startup| INFINISPAN

    style KAFKA_C fill:#4CAF50,color:#fff
    style SAT_R fill:#4CAF50,color:#fff
    style HIST_R fill:#4CAF50,color:#fff
    style WS_EP fill:#4CAF50,color:#fff
    style KAFKA_P fill:#4CAF50,color:#fff
    style ILHR fill:#4CAF50,color:#fff
    style QSU fill:#4CAF50,color:#fff
    style HSU fill:#4CAF50,color:#fff
    style P_QS fill:#4CAF50,color:#fff
    style P_HS fill:#4CAF50,color:#fff
    style P_LHR fill:#4CAF50,color:#fff
    style P_EVT fill:#4CAF50,color:#fff
    style LHIST fill:#4CAF50,color:#fff
    style BOOT fill:#4CAF50,color:#fff
    style SEC fill:#FF9800,color:#fff
    style OTEL fill:#FF9800,color:#fff
    style FT fill:#FF9800,color:#fff
    style METRICS fill:#FF9800,color:#fff
```

> **Verde** = Componentes nuevos | **Naranja** = Mejoras cross-cutting

---

## 3. Nuevo Microservicio: ms-satellite-tracker

### 3.1 Propuesta

El **ms-satellite-tracker** es un microservicio independiente que simula y gestiona la telemetria de los satelites en tiempo real. Su responsabilidad es:

1. **Gestionar las posiciones orbitales** de cada satelite (kenobi, skywalker, sato)
2. **Calcular la distancia** de cada satelite respecto a la nave detectada
3. **Emitir senales periodicas** hacia `ms-satelite-position-consumer` via Kafka
4. **Exponer una API REST** para configurar satelites, consultar orbitas y ajustar parametros

### 3.2 Contexto de negocio

```
Flujo actual (manual):
  Operador --> POST /topsecret_split/{name} --> Consumer calcula posicion

Flujo propuesto (automatizado):
  Tracker detecta nave --> calcula distancias --> publica en Kafka --> Consumer recibe y calcula
```

Esto elimina la dependencia del operador humano y permite tracking en tiempo real.

### 3.3 Diagrama de Componentes - ms-satellite-tracker

```mermaid
graph TB
    subgraph "Sistemas Externos"
        ADMIN[Admin / Operador]
        KAFKA[[Apache Kafka<br/>satellite-signals<br/>satellite-config]]
        POSTGRES[(PostgreSQL<br/>Orbitas<br/>Configuracion<br/>Historial detecciones)]
        PROMETHEUS[Prometheus]
    end

    subgraph "ms-satellite-tracker"
        subgraph "Infrastructure Layer - Adapters IN"
            SAT_API[SatelliteManagementResource<br/>POST /satellites<br/>GET /satellites<br/>PUT /satellites/name/orbit<br/>DELETE /satellites/name]
            ORBIT_API[OrbitResource<br/>GET /orbits/name/position?t=<br/>GET /orbits/name/trajectory]
            DETECT_API[DetectionResource<br/>POST /detections<br/>GET /detections/latest]
            CONFIG_API[ConfigResource<br/>PUT /config/scan-interval<br/>GET /config]
            KAFKA_CFG[KafkaConfigConsumer<br/>Canal: satellite-config]
            SCHED[ScheduledScanTask<br/>@Scheduled CRON]
        end

        subgraph "Application Layer - Use Cases"
            MSU[ManageSatelliteUseCaseImpl<br/>CRUD satelites]
            CSU[CalculateSignalUseCaseImpl<br/>Calcula distancia + fragmenta mensaje]
            DSU[DetectSpacecraftUseCaseImpl<br/>Detecta nave y dispara scan]
            SSU[ScheduledScanUseCaseImpl<br/>Scan periodico de todos los satelites]
            PSU[PublishSignalUseCaseImpl<br/>Publica senales a Kafka]
        end

        subgraph "Domain Layer"
            subgraph "Domain Services"
                OCS[OrbitCalculationService<br/>Calcula posicion orbital en tiempo T]
                DCS[DistanceCalculationService<br/>Distancia euclidiana sat-nave]
                MFS[MessageFragmentationService<br/>Fragmenta mensaje interceptado<br/>con gaps aleatorios]
            end

            subgraph "Ports IN"
                P_MS[ManageSatelliteUseCase]
                P_CS[CalculateSignalUseCase]
                P_DS[DetectSpacecraftUseCase]
                P_SS[ScheduledScanUseCase]
                P_PS[PublishSignalUseCase]
            end

            subgraph "Ports OUT"
                P_SR[SatelliteRepository]
                P_DR[DetectionRepository]
                P_SP[SignalPublisher]
            end

            subgraph "Model"
                SAT[Satellite<br/>name, orbitParams, status]
                ORB[OrbitalPosition<br/>x, y, timestamp]
                DET[Detection<br/>spacecraftCoords, timestamp, signalStrength]
                SIG[SatelliteSignal<br/>name, distance, messageParts]
                SCAN_CFG[ScanConfiguration<br/>intervalMs, enabled, satellites]
                ORBIT_P[OrbitParameters<br/>centerX, centerY, semiMajorAxis,<br/>semiMinorAxis, period, phase]
            end
        end

        subgraph "Infrastructure Layer - Adapters OUT"
            PG_SAT[PostgresSatelliteRepository]
            PG_DET[PostgresDetectionRepository]
            KAFKA_PUB[KafkaSignalPublisher<br/>Canal: satellite-signals]
        end

        subgraph "Infrastructure - Cross-Cutting"
            OTEL_T[OpenTelemetry Tracing]
            METRICS_T[Custom Metrics<br/>scans_total, detections_total<br/>signal_publish_latency]
            HC_T[HealthChecks<br/>Kafka connectivity<br/>PostgreSQL connectivity<br/>Scan scheduler status]
        end
    end

    ADMIN -->|HTTPS| SAT_API & ORBIT_API & DETECT_API & CONFIG_API
    KAFKA --> KAFKA_CFG

    SAT_API --> P_MS --> MSU --> P_SR
    ORBIT_API --> P_CS --> CSU --> OCS & DCS
    DETECT_API --> P_DS --> DSU --> P_DR & P_SS
    SCHED -->|@Scheduled| P_SS --> SSU --> P_CS
    SSU --> P_PS --> PSU --> P_SP

    CSU --> P_SR
    DSU --> P_SR
    MSU --> P_SR

    MFS -.->|Usado por| CSU

    P_SR --> PG_SAT --> POSTGRES
    P_DR --> PG_DET --> POSTGRES
    P_SP --> KAFKA_PUB --> KAFKA

    OTEL_T --> PROMETHEUS
    METRICS_T --> PROMETHEUS
```

### 3.4 Flujo de Datos Detallado - Scan Periodico

```mermaid
sequenceDiagram
    participant SCHED as ScheduledScanTask
    participant SSU as ScheduledScanUseCase
    participant CSU as CalculateSignalUseCase
    participant OCS as OrbitCalculationService
    participant DCS as DistanceCalculationService
    participant MFS as MessageFragmentationService
    participant SAT_REPO as SatelliteRepository
    participant DET_REPO as DetectionRepository
    participant KAFKA as KafkaSignalPublisher
    participant CONSUMER as ms-position-consumer

    SCHED->>SSU: executeScan()
    SSU->>DET_REPO: getLatestDetection()
    DET_REPO-->>SSU: Detection(nave: x=-100, y=-75)

    loop Para cada satelite (kenobi, skywalker, sato)
        SSU->>CSU: calculateSignal(satellite, detection)
        CSU->>SAT_REPO: findByName(name)
        SAT_REPO-->>CSU: Satellite(orbitParams)
        CSU->>OCS: calculatePosition(orbitParams, now)
        OCS-->>CSU: OrbitalPosition(x, y)
        CSU->>DCS: calculate(satPosition, navePosition)
        DCS-->>CSU: distance=447.213
        CSU->>MFS: fragment(interceptedMessage)
        MFS-->>CSU: ["", "este", "es", "", "mensaje"]
        CSU-->>SSU: SatelliteSignal(kenobi, 447.213, fragments)
    end

    SSU->>KAFKA: publish(List<SatelliteSignal>)
    KAFKA->>CONSUMER: satellite-signals topic
    Note over CONSUMER: KafkaSignalConsumer recibe<br/>y ejecuta RegisterSignalUseCase<br/>para cada senal
    CONSUMER->>CONSUMER: Si tiene 3 senales:<br/>calcula posicion via<br/>LocateFromCacheUseCase
```

### 3.5 Diagrama de Componentes - Ecosistema Completo

```mermaid
graph TB
    subgraph "Frontend / Operador"
        DASHBOARD[Dashboard Web<br/>React / Angular]
        ADMIN[Admin CLI]
    end

    subgraph "API Gateway"
        GW[Kong / Envoy<br/>- JWT Validation<br/>- Rate Limiting<br/>- Load Balancing<br/>- TLS Termination]
    end

    subgraph "Microservicios"
        subgraph "ms-satellite-tracker"
            TRACKER_API[REST API<br/>/satellites<br/>/orbits<br/>/detections<br/>/config]
            TRACKER_SCHED[Scheduled Scanner<br/>Cada N segundos]
            TRACKER_DOMAIN[Domain<br/>OrbitCalculation<br/>DistanceCalculation<br/>MessageFragmentation]
        end

        subgraph "ms-satelite-position-consumer"
            CONSUMER_API[REST API<br/>/topsecret<br/>/topsecret_split<br/>/satellites<br/>/locations/history]
            CONSUMER_KAFKA[Kafka Consumer<br/>satellite-signals]
            CONSUMER_WS[WebSocket<br/>/locations/stream]
            CONSUMER_DOMAIN[Domain<br/>Trilateration<br/>MessageReconstruction]
        end
    end

    subgraph "Messaging"
        KAFKA[[Apache Kafka]]
        KAFKA_SIGNALS[satellite-signals]
        KAFKA_LOCATIONS[location-events]
        KAFKA_CONFIG[satellite-config]
    end

    subgraph "Data Stores"
        POSTGRES[(PostgreSQL<br/>Satelites<br/>Orbitas<br/>Detecciones)]
        INFINISPAN[(Infinispan<br/>Posiciones satelite<br/>Senales nave<br/>Historial ubicaciones)]
    end

    subgraph "Observabilidad"
        JAEGER[Jaeger<br/>Distributed Tracing]
        PROMETHEUS[Prometheus<br/>Metrics Collection]
        GRAFANA[Grafana<br/>Dashboards & Alerts]
        LOKI[Loki<br/>Log Aggregation]
    end

    subgraph "Infrastructure - Azure"
        VM_1[Azure VM 1<br/>Consumer + Infinispan]
        VM_2[Azure VM 2<br/>Tracker + PostgreSQL]
        AKS[Azure Kubernetes<br/>Service - Futuro]
    end

    DASHBOARD -->|HTTPS| GW
    ADMIN -->|HTTPS| GW
    GW --> TRACKER_API
    GW --> CONSUMER_API
    GW --> CONSUMER_WS

    TRACKER_SCHED --> TRACKER_DOMAIN
    TRACKER_DOMAIN -->|Publica senales| KAFKA_SIGNALS
    TRACKER_API --> TRACKER_DOMAIN
    TRACKER_DOMAIN --> POSTGRES

    KAFKA_SIGNALS --> CONSUMER_KAFKA
    CONSUMER_KAFKA --> CONSUMER_DOMAIN
    CONSUMER_DOMAIN -->|Posicion calculada| KAFKA_LOCATIONS
    CONSUMER_API --> CONSUMER_DOMAIN
    CONSUMER_DOMAIN --> INFINISPAN
    KAFKA_LOCATIONS --> CONSUMER_WS
    KAFKA_LOCATIONS --> DASHBOARD

    TRACKER_DOMAIN -.->|Tracing| JAEGER
    CONSUMER_DOMAIN -.->|Tracing| JAEGER
    TRACKER_DOMAIN -.->|Metrics| PROMETHEUS
    CONSUMER_DOMAIN -.->|Metrics| PROMETHEUS
    PROMETHEUS --> GRAFANA
    TRACKER_DOMAIN -.->|Logs| LOKI
    CONSUMER_DOMAIN -.->|Logs| LOKI

    style TRACKER_API fill:#2196F3,color:#fff
    style TRACKER_SCHED fill:#2196F3,color:#fff
    style TRACKER_DOMAIN fill:#2196F3,color:#fff
    style CONSUMER_KAFKA fill:#4CAF50,color:#fff
    style CONSUMER_WS fill:#4CAF50,color:#fff
    style KAFKA fill:#FF9800,color:#fff
```

### 3.6 Modelo de Datos - ms-satellite-tracker

```mermaid
erDiagram
    SATELLITE {
        uuid id PK
        string name UK
        string status "ACTIVE, INACTIVE, MAINTENANCE"
        timestamp created_at
        timestamp updated_at
    }

    ORBIT_PARAMETERS {
        uuid id PK
        uuid satellite_id FK
        double center_x
        double center_y
        double semi_major_axis
        double semi_minor_axis
        double period_seconds
        double phase_radians
        timestamp effective_from
        timestamp effective_until "NULL = current"
    }

    DETECTION {
        uuid id PK
        double spacecraft_x
        double spacecraft_y
        double signal_strength
        string intercepted_message
        timestamp detected_at
    }

    SIGNAL_LOG {
        uuid id PK
        uuid detection_id FK
        uuid satellite_id FK
        double calculated_distance
        string message_fragment "JSON array"
        boolean published
        timestamp calculated_at
    }

    SATELLITE ||--o{ ORBIT_PARAMETERS : "tiene orbitas"
    DETECTION ||--o{ SIGNAL_LOG : "genera senales"
    SATELLITE ||--o{ SIGNAL_LOG : "emite desde"
```

### 3.7 API Contract - ms-satellite-tracker

```yaml
# Gestion de Satelites
POST   /api/v1/satellites                    # Registrar nuevo satelite
GET    /api/v1/satellites                    # Listar todos los satelites
GET    /api/v1/satellites/{name}             # Detalle de un satelite
PUT    /api/v1/satellites/{name}/orbit       # Actualizar parametros orbitales
PUT    /api/v1/satellites/{name}/status      # Activar/desactivar satelite
DELETE /api/v1/satellites/{name}             # Eliminar satelite

# Orbitas y Posiciones
GET    /api/v1/orbits/{name}/position        # Posicion actual del satelite
GET    /api/v1/orbits/{name}/position?t=ISO  # Posicion en momento T
GET    /api/v1/orbits/{name}/trajectory      # Trayectoria (lista de puntos)

# Detecciones de Nave
POST   /api/v1/detections                    # Registrar deteccion manual de nave
GET    /api/v1/detections/latest             # Ultima deteccion
GET    /api/v1/detections?from=&to=          # Historial de detecciones

# Configuracion del Scanner
GET    /api/v1/config                        # Config actual del scanner
PUT    /api/v1/config/scan-interval          # Cambiar intervalo de escaneo
PUT    /api/v1/config/scan-enabled           # Activar/desactivar escaneo

# Health & Monitoring
GET    /q/health/live                        # Liveness
GET    /q/health/ready                       # Readiness (Kafka + PostgreSQL)
GET    /q/metrics                            # Prometheus metrics
```

### 3.8 Stack Tecnologico Propuesto - ms-satellite-tracker

| Componente | Tecnologia | Justificacion |
|------------|-----------|---------------|
| Runtime | Quarkus 3.33.1 LTS + JDK 25 | Consistencia con consumer, startup rapido |
| Base de datos | PostgreSQL 16 | Datos relacionales (satelites, orbitas, historial) |
| ORM | Quarkus Hibernate ORM + Panache | Simplifica CRUD, compatible con native |
| Messaging | Quarkus SmallRye Reactive Messaging (Kafka) | Comunicacion asincrona con consumer |
| Scheduling | Quarkus Scheduler (`@Scheduled`) | Escaneo periodico sin dependencias externas |
| Tracing | OpenTelemetry | Trazabilidad end-to-end con consumer |
| Metrics | Micrometer + Prometheus | Metricas custom del scanner |
| Testing | JUnit 5 + Testcontainers (PostgreSQL + Kafka) | Tests de integracion realistas |
| API Docs | SmallRye OpenAPI | Swagger UI |
| Seguridad | SmallRye JWT | Mismo proveedor de tokens que consumer |

### 3.9 Comunicacion entre Microservicios

```mermaid
graph LR
    subgraph "ms-satellite-tracker"
        T_PUB[KafkaSignalPublisher]
        T_SUB[KafkaConfigConsumer]
    end

    subgraph "Apache Kafka"
        T1[satellite-signals<br/>Partitions: 3<br/>Retention: 24h]
        T2[location-events<br/>Partitions: 1<br/>Retention: 7d]
        T3[satellite-config<br/>Partitions: 1<br/>Compacted]
    end

    subgraph "ms-position-consumer"
        C_SUB[KafkaSignalConsumer]
        C_PUB[KafkaLocationPublisher]
    end

    T_PUB -->|SatelliteSignal JSON| T1
    T1 -->|Consumer Group: position-consumer| C_SUB
    C_PUB -->|LocationEvent JSON| T2
    T2 -->|Consumer Group: tracker-feedback| T_SUB
    T3 -->|Config updates| T_SUB

    style T1 fill:#FF9800,color:#fff
    style T2 fill:#4CAF50,color:#fff
    style T3 fill:#2196F3,color:#fff
```

**Formato del mensaje Kafka (satellite-signals):**
```json
{
  "eventId": "uuid",
  "timestamp": "2026-04-18T10:30:00Z",
  "detectionId": "uuid",
  "satellite": {
    "name": "kenobi",
    "distance": 447.213,
    "message": ["", "este", "es", "", "mensaje"]
  }
}
```

**Formato del mensaje Kafka (location-events):**
```json
{
  "eventId": "uuid",
  "timestamp": "2026-04-18T10:30:01Z",
  "position": { "x": -81.25, "y": -112.50 },
  "message": "este es un mensaje secreto",
  "satellitesUsed": ["kenobi", "sato", "skywalker"]
}
```

---

## 4. Diagrama de Despliegue

```mermaid
graph TB
    subgraph "GitHub"
        GH_ACTIONS[GitHub Actions CI/CD]
        GHCR[GitHub Container Registry<br/>ghcr.io]
    end

    subgraph "Azure Cloud"
        subgraph "Azure VM 1 - Consumer"
            DOCKER_1[Docker Engine]
            C_CONTAINER[ms-position-consumer<br/>:8080]
            I_CONTAINER[Infinispan Server<br/>:11222]
            C_CONTAINER ---|quasar-net| I_CONTAINER
        end

        subgraph "Azure VM 2 - Tracker"
            DOCKER_2[Docker Engine]
            T_CONTAINER[ms-satellite-tracker<br/>:8081]
            PG_CONTAINER[PostgreSQL 16<br/>:5432]
            T_CONTAINER ---|tracker-net| PG_CONTAINER
        end

        subgraph "Azure VM 3 - Messaging & Observability"
            K_CONTAINER[Apache Kafka<br/>:9092]
            ZK_CONTAINER[Zookeeper<br/>:2181]
            PROM_CONTAINER[Prometheus<br/>:9090]
            GRAF_CONTAINER[Grafana<br/>:3000]
            JAEGER_CONTAINER[Jaeger<br/>:16686]
        end
    end

    GH_ACTIONS -->|Build & Push| GHCR
    GHCR -->|docker pull| DOCKER_1 & DOCKER_2

    T_CONTAINER -->|Publish signals| K_CONTAINER
    K_CONTAINER -->|Consume signals| C_CONTAINER
    C_CONTAINER -->|Publish locations| K_CONTAINER

    C_CONTAINER -.->|metrics| PROM_CONTAINER
    T_CONTAINER -.->|metrics| PROM_CONTAINER
    C_CONTAINER -.->|traces| JAEGER_CONTAINER
    T_CONTAINER -.->|traces| JAEGER_CONTAINER
    PROM_CONTAINER --> GRAF_CONTAINER
```

---

## 5. Resumen de Esfuerzo Estimado

| Componente | Esfuerzo | Prioridad |
|------------|----------|-----------|
| Mejoras consumer (Fault Tolerance, Validacion, Metrics) | 3-5 dias | Alta |
| Kafka integration en consumer (adapter in) | 2-3 dias | Alta |
| WebSocket + Location History en consumer | 2-3 dias | Media |
| ms-satellite-tracker - Domain + Use Cases | 5-7 dias | Alta |
| ms-satellite-tracker - Infrastructure (Kafka, PostgreSQL) | 3-5 dias | Alta |
| ms-satellite-tracker - API REST + Tests | 3-4 dias | Alta |
| Observabilidad (OpenTelemetry, Grafana) | 2-3 dias | Media |
| API Gateway + Seguridad JWT | 2-3 dias | Media |
| **Total estimado** | **22-33 dias** | |
