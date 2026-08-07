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
STG_ISSUER_URI=http://localhost:8080/realms/ecommerce \
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
| `stg` | OAuth2 + JWT (`ROLE_ADMIN` o `SCOPE_catalog:write`) | Detrás de gateway con Keycloak |
| `prod` | OAuth2 + JWT (`ROLE_ADMIN` o `SCOPE_catalog:write`) | Producción, Swagger deshabilitado |

#### Build completo
```bash
# Compilar el proyecto entero y correr los tests
./gradlew build jacocoTestReport
```

> **Nota:** Los reportes JaCoCo se generan correctamente con Java 25. JaCoCo solo
> está deshabilitado en la tarea `intTest` (`infrastructure/build.gradle`) para
> evitar problemas del agente JVM en los tests de integración.

### 4. Datos de prueba

Al usar `./gradlew :boot:bootRun` (perfil `dev`), el `DataSeeder` inserta automáticamente 3 productos en estado `ACTIVE`:

| Producto | Variantes | Rango de precios |
|---|---|---|
| iPhone 16 Pro Max | 5 (colores / almacenamiento) | $1,799 – $2,299 |
| MacBook Pro 16" M4 Max | 4 (RAM / SSD) | $3,499 – $4,999 |
| Sony WH-1000XM6 | 3 (colores) | $449 |

Cada producto incluye variantes con stock diferenciado, imágenes, atributos técnicos, categorías y tags.

> El seeder solo se ejecuta si la base de datos está vacía (`findAll().totalElements() == 0`). Si ya hay productos, los salta sin duplicar.

## 🔐 Autenticación y autorización

> Documento completo: [`docs/authentication.md`](docs/authentication.md) (roles, usuarios, clientes de Keycloak, flujos por llamador y matriz endpoint × llamador).
>
> La definición del realm `ecommerce` se gestiona en el proyecto **`keycloak-local`**
> (`realm/ecommerce-realm.json`, Keycloak en Docker); no vive en este repositorio.

El servicio actúa como **OAuth2 Resource Server** con validación de **JWT** (stateless, RFC 7519). Spring Security valida firma, expiración, `iss` y `aud` contra el proveedor (Keycloak) descubriendo la configuración y las claves JWKS automáticamente a partir de `spring.security.oauth2.resourceserver.jwt.issuer-uri`. No se hacen llamadas de introspectión por request.

### Mapeo de claims a authorities

Un `JwtAuthenticationConverter` personalizado (`JwtAuthenticationConfig`) combina dos fuentes de authorities:

| Claim | Authority resultante | Origen |
|---|---|---|
| `scope` / `scp` | `SCOPE_<valor>` (ej. `SCOPE_catalog:write`) | Converter por defecto de Spring Security |
| `realm_access.roles` | `ROLE_<rol>` (ej. `ROLE_ADMIN`) | Roles del realm de Keycloak |

Gracias a este mapeo, la regla de escritura funciona tanto con tokens de usuario
(`ROLE_ADMIN`) como con tokens machine-to-machine (`SCOPE_catalog:write`). El
servicio también valida que el claim `aud` contenga `catalog-service` (tolerando
`String` o `List<String>`), para rechazar tokens emitidos para otra API. Los roles
`CUSTOMER`, `SELLER` y `SUPPORT` solo cuentan como "token válido"; son para el
storefront u otros microservicios.

### Reglas de autorización por ruta

| Ruta | Acceso |
|---|---|
| `GET /api/v1/products/**` | Público (catálogo de lectura) |
| `POST/PUT/DELETE/PATCH /api/v1/products/**` | `ROLE_ADMIN` **o** `SCOPE_catalog:write` |
| `/management/**` (actuator) | Público |
| `/v3/api-docs/**`, `/swagger-ui/**` | Público (solo si está habilitado) |
| Cualquier otra | Requiere token válido (`authenticated`) |

### Obtención de tokens y endpoints permitidos

**1. Token de usuario con rol** (backoffice / probar roles) — `grant_type=password` sobre el cliente confidencial `ecommerce-api`:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/realms/ecommerce/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" -d "client_id=ecommerce-api" \
  -d "client_secret=${ECOM_API_CLIENT_SECRET}" \
  -d "username=admin" -d "password=admin123" | jq -r .access_token)
```

*(usuarios de prueba: `admin`, `customer1`, `seller1`, `support1`)*

**2. Token machine-to-machine** (servicio/eventos) — `grant_type=client_credentials`:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/realms/ecommerce/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" -d "client_id=ecommerce-api" \
  -d "client_secret=${ECOM_API_CLIENT_SECRET}" | jq -r .access_token)
```

**3. Qué token usar según el endpoint:**

| Endpoint | Método | Sin token | Usuario sin ADMIN | Usuario ADMIN | Máquina `catalog:read` | Máquina `catalog:write` |
|---|---|---|---|---|---|---|
| `/api/v1/products` | GET | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/api/v1/products/{id}` | GET | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/api/v1/products/slug/{slug}` | GET | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/api/v1/products/variants/{variantId}/availability` | GET | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/api/v1/products` | POST | ❌ 401 | ❌ 403 | ✅ | ❌ 403 | ✅ |
| `/api/v1/products/{id}` | PUT | ❌ 401 | ❌ 403 | ✅ | ❌ 403 | ✅ |
| `/api/v1/products/{id}/activate` | PUT | ❌ 401 | ❌ 403 | ✅ | ❌ 403 | ✅ |
| `/api/v1/products/{id}` | DELETE | ❌ 401 | ❌ 403 | ✅ | ❌ 403 | ✅ |
| `/api/v1/products/{id}/variants/{variantId}/stock` | PATCH | ❌ 401 | ❌ 403 | ✅ | ❌ 403 | ✅ |

**Reglas:**
- **Lectura (GET):** público, no requiere token.
- **Escritura (POST/PUT/DELETE/PATCH):** requiere `ROLE_ADMIN` (usuario `admin`)
  **o** `SCOPE_catalog:write` (service account de `ecommerce-api` con ese client
  scope). Un token de `customer1`/`seller1`/`support1` o una máquina con solo
  `catalog:read` da **403**.
- **401** = falta el token o no es válido (incluye `aud` incorrecto); **403** = token válido pero sin permiso.

**Ejemplo de uso:**
```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{...}'
```

**Nota de buenas prácticas (entornos desplegados):** en local (perfiles `dev`/`stg`) los atajos son aceptables (p. ej. `grant_type=password`, client secret en el archivo de realm, redirects/origins abiertos). Si `stg` se despliega como entorno de QA, debe aplicar el mismo endurecimiento que `prod`: sin `grant_type=password` (usar authorization code + PKCE en el frontend), secret inyectado por variable de entorno, y redirects/origins restringidos.

### Integración con Spring Cloud Gateway

El diseño es **totalmente compatible** con un API Gateway (p. ej. Spring Cloud Gateway):

- **Validación stateless:** al validar el JWT por sí mismo, el servicio no necesita estado de sesión ni contactar al auth server por cada request; funciona transparente detrás de cualquier gateway (defensa en profundidad).
- **Token Relay:** el gateway reenvía el header `Authorization: Bearer` entrante. En Spring Cloud Gateway se activa con `TokenRelayGatewayFilterFactory` (`token-relay: true`), o simplemente pasando el header hacia el downstream.
- **CORS:** el servicio no define CORS; se gestiona en el edge (gateway).
- **Rutas:** el gateway debe **preservar el prefijo `/api/v1`** (evitar `StripPrefix`/rewrite sin ajustar las reglas de autorización del servicio).
- **Issuer:** el servicio debe poder alcanzar el `issuer-uri` directamente (fetch de JWKS); no es necesario exponer Keycloak a través del gateway.

> **Importante:** nunca desplegar con el perfil `dev` detrás de un gateway: en `dev` la seguridad está deshabilitada (`permitAll`).

## 📚 Documentación Interactiva de API (Swagger)
Una vez que el servidor Spring Boot esté corriendo, puedes explorar, leer y probar todos los endpoints disponibles a través de **Swagger UI**:

🌐 **[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)**

> El botón **Authorize** aparece solo en `stg` (security activo). En `dev` no se solicita token. En `prod` Swagger está deshabilitado. El botón **Try it out** ejecuta contra `openapi.server.url` (en `stg` local por defecto `http://localhost:8081`, el servicio). Keycloak vive en `8080`; no confundir con el servicio (`8081`).

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
./gradlew ciTest -DdoIntegrationTest=true
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

### [2026-08-07] — Realm en keycloak-local, tests alineados a TESTING_RULES.md y fix CI
- **Realm movido:** La definición del realm `ecommerce` vive ahora en el proyecto `keycloak-local` (`realm/ecommerce-realm.json`), alineado con el modelo del servicio (`aud` = `catalog-service`, scopes `catalog:read`/`catalog:write`, roles `ADMIN`/`CUSTOMER`/`SELLER`/`SUPPORT`).
- **Tests alineados:** `SecurityConfigTest`, `SecurityAuthorizationTest`, `JwtAuthenticationConverterTest`, `ResourceServerJwtConfigTest` y `FilterConfigTest` reformateados según `TESTING_RULES.md` (naming `methodUnderTestingName_StateUnderTest`, Javadoc en español, estructura `//given//when//then`, helpers en `editor-fold`).
- **Fix `ciTest`:** Corregido `build.gradle` (la dependencia sobre los tasks `test` de subproyectos usaba `*.matching`, que rompía `./gradlew tasks` y `ciTest`).
- **Cobertura:** Verificado mínimo 80% en los tres módulos con tests (domain 92%, application 90%, infrastructure 93%).

### [2026-08-04] — Mapeo de roles Keycloak y documentación de seguridad
- **Corregido `hasRole("ADMIN")`:** El converter por defecto de Spring Security solo mapeaba `scope` → `SCOPE_*`, por lo que los endpoints de escritura devolvían `403` siempre. Nuevo `JwtAuthenticationConfig` combina `scope` (`SCOPE_*`) con `realm_access.roles` de Keycloak (`ROLE_<rol>`).
- **Issuer Keycloak moderno:** `STG_ISSUER_URI` por defecto actualizado a `http://localhost:8080/realms/ecommerce` (Keycloak 18+, sin `/auth`).
- **Tests:** Nuevos `JwtAuthenticationConverterTest` (unitario) y `SecurityAuthorizationTest` (200/401/403 con `JwtDecoder` mockeado); `SecurityConfigTest` valida el nuevo bean.
- **Documentación:** Nueva sección "Autenticación y autorización" en el README y ADR en `docs/prd.md` sobre autenticación OAuth2/JWT e integración con Spring Cloud Gateway.

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
*Este servicio está preparado para Integración Continua (CI), contando con centralización de variables en Gradle, Toolchains de Java 25 y Reportes de Cobertura JaCoCo (mínimo 80% en unit tests de todos los módulos, verificado en el último build: domain 92%, application 90%, infrastructure 93%).*


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
