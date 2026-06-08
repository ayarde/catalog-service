# Catalog Service

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.2-DC382D?style=for-the-badge&logo=redis&logoColor=white)

El **Catalog Service** es el núcleo de lectura del sistema E-commerce. Actúa como la fuente central de verdad para productos, categorías, inventario y variantes (sub-SKUs).
Está desarrollado con **Arquitectura Hexagonal (Ports and Adapters)**, logrando un desacoplamiento estricto entre el dominio de negocio y la infraestructura.

## 🚀 Tecnologías Destacadas
- **Java Virtual Threads:** Soporte nativo de alta concurrencia (Proyecto Loom) habilitado por defecto.
- **Caché Distribuido:** Uso de **Redis** y Spring Cache para devolver lecturas masivas en `<50ms` y proteger a la base de datos de sobrecargas. Invalidación reactiva instantánea.
- **Resiliencia (Resilience4j):** Protección activa de endpoints mediante *Rate Limiters* y *Bulkheads* para mitigar picos anómalos o intentos DDoS.
- **Base de Datos NoSQL:** **MongoDB** es el almacenamiento transaccional para modelar documentos anidados (Agregados), evitando múltiples y pesados JOINs relacionales.

## 📁 Documentación de Diseño
Las decisiones de arquitectura y casos de uso detallados se encuentran en el Product Requirements Document (PRD):
👉 **[Ver PRD Completo](docs/prd.md)**

## ⚙️ Cómo ejecutar en local

### 1. Iniciar Infraestructura Dockerizada
El servicio requiere MongoDB, Redis, RabbitMQ y Zipkin corriendo en Docker.
```bash
docker-compose -f docker-compose-infra.yml up -d
```

### 2. Variables de Entorno (.env local)
Por defecto, la aplicación toma las credenciales locales estándar que genera Docker Compose (ver `application.yml` para referencias directas).

### 3. Compilar y Arrancar
Como es un proyecto multi-módulo en Gradle, arrancaremos desde el módulo `:boot` que ensambla todas las capas.
```bash
# Compilar el proyecto entero y correr los tests
./gradlew build jacocoTestReport

# Abrir el reporte HTML (unit tests) del módulo 'application'
open file:///Users/adrian/Develop/catalog-service/application/build/reports/jacoco/test/html/index.html
```

> **Nota:** Actualmente los reportes de JaCoCo están desactivados debido a incompatibilidades con Java 25. Para habilitarlos, cambie la toolchain a Java 21 (o una versión compatible) y elimine la línea que desactiva `jacoco.enabled` en `build.gradle`.

> El servicio se ejecutará de forma predeterminada en el puerto `8080`.

## 📚 Documentación Interactiva de API (Swagger)
Una vez que el servidor Spring Boot esté corriendo, puedes explorar, leer y probar todos los endpoints disponibles a través de **Swagger UI**:

🌐 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

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
| `./gradlew testAll` | Sí (domain, application, infrastructure) | No | No |
| `./gradlew intTestAll -DdoIntegrationTest=true` | No | Sí | Sí |
| `./gradlew :infrastructure:intTest -DdoIntegrationTest=true` | No | Sí | Sí |

El flag **`-DdoIntegrationTest=true`** activa `intTest` en `check` y por tanto en `build`. Sin el flag, `intTest` aparece como `⏭ SKIPPED` y no levanta contenedores.

### Unit tests (sin Docker)

`testAll` ejecuta las tareas **en serie** (1/3 → 2/3 → 3/3) con banner por módulo Gradle y progreso numerado por test (`[01] ▷` / `[01] ✓`).

```bash
# Un módulo
./gradlew :domain:test
./gradlew :application:test
./gradlew :infrastructure:test

# Los tres módulos
./gradlew testAll

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
*Este servicio está preparado para Integración Continua (CI), contando con centralización de variables en Gradle, Toolchains de Java 25 y Reportes de Cobertura JaCoCo (mínimo 80% en unit tests del módulo infrastructure).*
