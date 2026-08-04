# Catalog Service

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.2-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Security](https://img.shields.io/badge/Security-OAuth2_JWT-000000?style=for-the-badge)

El **Catalog Service** es el núcleo de lectura del sistema E-commerce. Actúa como la fuente central de verdad para productos, categorías, inventario y variantes (sub-SKUs).
Está desarrollado con **Arquitectura Hexagonal (Ports and Adapters)**, logrando un desacoplamiento estricto entre el dominio de negocio y la infraestructura.

## 🚀 Tecnologías Destacadas
- **Java Virtual Threads:** Soporte nativo de alta concurrencia (Proyecto Loom) habilitado por defecto.
- **Caché Distribuido:** Uso de **Redis** y Spring Cache para devolver lecturas masivas en `<50ms` y proteger a la base de datos de sobrecargas. Invalidación reactiva instantánea.
- **Resiliencia (Resilience4j):** Protección activa de endpoints mediante *Circuit Breakers*, *Rate Limiters* y *Bulkheads* para mitigar picos anómalos o intentos DDoS.
- **Base de Datos NoSQL:** **MongoDB** es el almacenamiento transaccional para modelar documentos anidados (Agregados), evitando múltiples y pesados JOINs relacionales.

## 📁 Documentación de Diseño
Las decisiones de arquitectura y casos de uso detallados se encuentran en el Product Requirements Document (PRD):
👉 **[Ver PRD Completo](docs/prd.md)**

## ⚙️ Cómo ejecutar en local

### 1. Iniciar Infraestructura Dockerizada
El servicio requiere MongoDB, Redis y RabbitMQ corriendo en Docker. Zipkin, Prometheus y Grafana están en un proyecto compartido ([ecommerce-observability](https://github.com/.../ecommerce-observability)).

```bash
# Infraestructura del servicio (MongoDB, Redis, RabbitMQ)
docker-compose -f docker-compose-infra.yml up -d
```

### 2. Variables de Entorno (.env local)
Por defecto, la aplicación toma las credenciales locales estándar que genera Docker Compose (ver `application.yml` para referencias directas).

### 3. Compilar y Arrancar

#### Desarrollo local (hot reload)
```bash
# Iniciar con perfil dev (datos de prueba automáticos)
./gradlew :boot:bootRun
```

El perfil `dev` se activa automáticamente, lo que ejecuta el `DataSeeder` al iniciar. Si la base de datos está vacía, se insertan 3 productos de ejemplo con variantes, imágenes y atributos realistas (ver [Datos de prueba](#datos-de-prueba)).

#### Con Spring Cloud Gateway + Keycloak (seguridad activa)

```bash
# Staging local con OAuth2 (requiere Keycloak en Docker)
STG_ISSUER_URI=http://localhost:8080/auth/realms/catalog \
  ./gradlew :boot:bootRun --args='--spring.profiles.active=stg'
```

#### Producción

```bash
# Requiere PROD_ISSUER_URI configurado en el entorno
java -jar boot/build/libs/catalog-service-*.jar --spring.profiles.active=prod
```

| Perfil | Seguridad | Uso |
|--------|-----------|-----|
| `dev` (default) | Deshabilitada | Desarrollo standalone, sin gateway |
| `stg` | OAuth2 + rol ADMIN | Detrás de gateway con Keycloak |
| `prod` | OAuth2 + rol ADMIN | Producción, Swagger deshabilitado |

#### Build completo
```bash
# Compilar el proyecto entero y correr los tests
./gradlew build jacocoTestReport
```

> **Nota:** Actualmente los reportes de JaCoCo están desactivados debido a incompatibilidades con Java 25. Para habilitarlos, cambie la toolchain a Java 21 (o una versión compatible) y elimine la línea que desactiva `jacoco.enabled` en `build.gradle`.

### 4. Datos de prueba

Al usar `./gradlew :boot:bootRun` (perfil `dev`), el `DataSeeder` inserta automáticamente 3 productos en estado `ACTIVE`:

| Producto | Variantes | Rango de precios |
|---|---|---|
| iPhone 16 Pro Max | 5 (colores / almacenamiento) | $1,799 – $2,299 |
| MacBook Pro 16" M4 Max | 4 (RAM / SSD) | $3,499 – $4,999 |
| Sony WH-1000XM6 | 3 (colores) | $449 |

Cada producto incluye variantes con stock diferenciado, imágenes, atributos técnicos, categorías y tags.

> El seeder solo se ejecuta si la base de datos está vacía (`findAll().totalElements() == 0`). Si ya hay productos, los salta sin duplicar.

## 📚 Documentación Interactiva de API (Swagger)
Una vez que el servidor Spring Boot esté corriendo, puedes explorar, leer y probar todos los endpoints disponibles a través de **Swagger UI**:

🌐 **[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)**

> El botón **Authorize** aparece solo en `stg` (security activo). En `dev` no se solicita token. En `prod` Swagger está deshabilitado.

---
## 🧪 Tests

### Qué ejecuta cada comando

| Comando | Unit tests (`test`) | Integration tests (`intTest`) | Logs Testcontainers (`STANDARD_OUT`) |
|---------|---------------------|-------------------------------|--------------------------------------|
| `./gradlew build` | Sí (`check` → `test`) | No (`intTest` SKIPPED) | No |
| `./gradlew clean build` | Sí | No | No |
| `./gradlew build -DdoIntegrationTest=true` | Sí | Sí (`check` → `intTest`) | Sí |
| `./gradlew clean build -DdoIntegrationTest=true` | Sí | Sí | Sí |
| `./gradlew :infrastructure:build -DdoIntegrationTest=true` | Sí (módulo) | Sí (módulo) | Sí |
| `./gradlew :infrastructure:intTest -DdoIntegrationTest=true` | No | Sí | Sí |

El flag **`-DdoIntegrationTest=true`** activa `intTest` en `check` y por tanto en `build`. Sin el flag, `intTest` aparece como `⏭ SKIPPED` y no levanta contenedores.

### Unit tests (sin Docker)

```bash
# Un módulo
./gradlew :domain:test
./gradlew :application:test
./gradlew :infrastructure:test

# Los tres módulos
./gradlew :domain:test :application:test :infrastructure:test

# Build completo (unit tests en cada módulo vía check)
./gradlew build
./gradlew clean build

# Cobertura (infrastructure)
./gradlew :infrastructure:jacocoTestReport :infrastructure:jacocoTestCoverageVerification
```

### Integration tests (requiere Docker en ejecución)

Los `intTest` muestran salida detallada: `ClassName STANDARD_OUT` con logs de Testcontainers (`INFO`/`ERROR`), más progreso `[01] ▷` / `[01] ✓` por método.

**Requisitos:**
- Docker Desktop o Docker Engine **en ejecución** (`docker info` debe responder sin error)
- **Docker Engine ≥ 29** requiere **Testcontainers ≥ 1.21.4** (configurado en `:infrastructure`)
- Sin Docker, los tests se **omitirán** (`@Testcontainers(disabledWithoutDocker = true)`) en lugar de fallar el build

**Imágenes usadas** (alineadas con `docker-compose-infra.yml`):
- MongoDB: `mongo:7.0`
- RabbitMQ: `rabbitmq:3.12-management-alpine`

```bash
# Build con unit + integration tests
./gradlew build -DdoIntegrationTest=true
./gradlew clean build -DdoIntegrationTest=true

# Solo integration (un módulo)
./gradlew :infrastructure:cleanIntTest :infrastructure:intTest -DdoIntegrationTest=true

# Application + infrastructure
./gradlew intTestAll -DdoIntegrationTest=true
```

> **Nota:** Las tareas `test` e `intTest` **siempre re-ejecutan** (no usan caché UP-TO-DATE).
> En `intTest` verás logs de `org.testcontainers.*` en tiempo real bajo `STANDARD_OUT`.
> La tarea `intTest` exporta `DOCKER_API_VERSION=1.44` como respaldo para Docker Engine 29+.

**CI / troubleshooting:** si Testcontainers no detecta el daemon, verifica `DOCKER_HOST`. En macOS suele ser el socket de Docker Desktop.

---
## 📊 Observabilidad y Métricas

### Stack compartido (`ecommerce-observability`)

Zipkin, Prometheus y Grafana corren en un proyecto Docker independiente para que todos los microservicios del ecosistema compartan el mismo stack:

```bash
git clone <repo>/ecommerce-observability.git
cd ecommerce-observability
docker compose up -d
```

| Servicio | URL | Propósito |
|---|---|---|
| **Zipkin** | `http://localhost:9411` | Trazabilidad distribuida |
| **Prometheus** | `http://localhost:9090` | Almacenamiento de métricas |
| **Grafana** | `http://localhost:3000` | Dashboards (admin/admin) |

Catalog-service se conecta a Zipkin vía la variable `ZIPKIN_BASE_URL` (default `http://localhost:9411`).

### Endpoint de métricas

Spring Boot Actuator expone las métricas en formato Prometheus en:

```
GET http://localhost:8081/management/prometheus
```

### Métricas custom implementadas

Además de las métricas automáticas de JVM, MongoDB, RabbitMQ y HTTP, el servicio expone métricas de negocio y resiliencia:

| Métrica | Tipo | Descripción |
|---|---|---|
| `catalog_products_created_total` | Counter | Total de productos creados |
| `catalog_products_activated_total` | Counter | Total de productos activados |
| `catalog_validation_duplicate_sku_total` | Counter | Intentos de SKU duplicado |
| `catalog_stock_low_stock` | Gauge | Productos con stock bajo el umbral |
| `catalog_stock_out_of_stock` | Gauge | Productos agotados (OUT_OF_STOCK) |
| `catalog_events_publish_failed_total` | Counter | Fallos al publicar eventos en RabbitMQ |
| `catalog_create_seconds` | Timer (p99) | Tiempo de creación de producto |
| `catalog_update_stock_seconds` | Timer (p99) | Tiempo de actualización de stock |
| `resilience4j_circuitbreaker_state` | Gauge | Estado del Circuit Breaker |
| `resilience4j_ratelimiter_available_permissions` | Gauge | Permisos disponibles del Rate Limiter |

### Verificación rápida

```bash
# Endpoint de métricas
curl -s http://localhost:8081/management/prometheus | rg 'catalog_|resilience4j'

# Trazas en Zipkin
curl -s http://localhost:9411/api/v2/services
```

---
## 📋 Changelog de cambios recientes

### [2026-07-21] — Métricas custom, MetricsConfig y Zipkin movido a proyecto compartido
- **Zipkin movido:** El contenedor Zipkin se eliminó de `docker-compose-infra.yml` y ahora forma parte del proyecto compartido `ecommerce-observability` (junto con Prometheus y Grafana).
- **`ZIPKIN_BASE_URL`:** Añadida configuración vía variable de entorno en todos los perfiles. Default `localhost:9411` para desarrollo local.
- **`MetricsConfig.java`:** Nueva clase de configuración que registra tags globales y exporta métricas de Resilience4j (Circuit Breaker, Rate Limiter) a Prometheus.
- **Métricas de negocio:** `ProductService` expone contadores de creación/activación, SKUs duplicados, stock bajo/agotado, y timers con percentil 99 para creación y actualización de stock.
- **Métricas de mensajería:** `RabbitEventPublisher` expone contador de eventos fallidos al publicar.
- **Tests:** Nuevos `ProductServiceMetricsTest` y `RabbitEventPublisherMetricsTest` siguiendo `TESTING_RULES.md`.

### [2026-06-08] — DataSeeder, corrección de caché Redis y mejoras en logs
- **DataSeeder:** Nuevo `DataSeeder` activo con perfil `dev` que inserta 3 productos realistas al iniciar la app.
- **Perfil `dev` por defecto:** `spring.profiles.active: dev` configurado en `application.yml`. `./gradlew :boot:bootRun` ya levanta con datos de prueba.
- **Corrección serialización Redis:** Cambio de `DefaultTyping.NON_FINAL` a `DefaultTyping.EVERYTHING` en `RedisCacheConfig` para que Jackson incluya `@class` en records de Java. Soluciona `SerializationException` al leer la caché de `products_list`.
- **Mejora en logs de fallback:** El método `listProductsFallback` ahora loguea la excepción real (`Fallback triggered ... due to {}: {}`) en lugar del mensaje genérico "Rate Limit exceeded".
- **Puerto Swagger corregido:** Documentación apunta a `8081` (era `8080`).

---
*Este servicio está preparado para Integración Continua (CI), contando con centralización de variables en Gradle, Toolchains de Java 25 y Reportes de Cobertura JaCoCo (mínimo 80% en unit tests del módulo infrastructure).*


## Versionado

El proyecto usa [Semantic Versioning](https://semver.org) con soporte para snapshots.

### Comandos disponibles

| Comando | Descripcion |
|---------|-------------|
| `./gradlew printVersion` | Muestra la version actual del proyecto |
| `./gradlew bumpMajor` | Incrementa major (1.0.0 -> 2.0.0-SNAPSHOT) |
| `./gradlew bumpMinor` | Incrementa minor (1.0.0 -> 1.1.0-SNAPSHOT) |
| `./gradlew bumpPatch` | Incrementa patch (1.0.0 -> 1.0.1-SNAPSHOT) |
| `./gradlew release` | Quita -SNAPSHOT (1.0.0-SNAPSHOT -> 1.0.0) |
| `./gradlew snapshot` | Agrega -SNAPSHOT si no lo tiene |
| `./gradlew -Pversion=X.Y.Z bootJar` | Build con version custom |
| `./gradlew :boot:dockerBuild` | Construye imagen Docker local |
| `./gradlew bootJar` | Genera JAR ejecutable en build/libs/ |

### Ejemplos de uso

```bash
# Version custom para testing
./gradlew -Pversion=1.0.0-RC1 bootJar

# Release completo
./gradlew bumpPatch && ./gradlew release && ./gradlew bootJar

# Imagen Docker
./gradlew -Pversion=1.0.0 :boot:dockerBuild
# -> docker image catalog-service:1.0.0
```

<!-- end-versioning -->
