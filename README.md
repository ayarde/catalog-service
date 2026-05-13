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

# Levantar el servidor
./gradlew :boot:bootRun
```
> El servicio se ejecutará de forma predeterminada en el puerto `8080`.

## 📚 Documentación Interactiva de API (Swagger)
Una vez que el servidor Spring Boot esté corriendo, puedes explorar, leer y probar todos los endpoints disponibles a través de **Swagger UI**:

🌐 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

---
*Este servicio está preparado para Integración Continua (CI), contando con centralización de variables en Gradle, Toolchains de Java 25 y Reportes de Cobertura JaCoCo.*
