# Product Requirements Document (PRD): Catalog Service

## 1. Visión del Producto
El **Catalog Service** es el repositorio central (fuente de verdad) de todos los productos del E-commerce. Dado que la inmensa mayoría del tráfico de una tienda online es de lectura (usuarios navegando vitrinas y buscando productos), este microservicio está diseñado con una arquitectura hiper-optimizada para la **lectura masiva y la alta disponibilidad**, desacoplándose de los cuellos de botella transaccionales.

## 2. Objetivos y KPIs
- **Alto Rendimiento (Lectura):** Soportar miles de lecturas concurrentes con respuestas en submilisegundos (latencia < 50ms) usando Caché Distribuido.
- **Escalabilidad Concurrente:** Maximizar el throughput del servidor utilizando *Java Virtual Threads*.
- **Resiliencia:** Aplicar patrones como *Rate Limiting* y *Bulkhead* para evitar la saturación del sistema frente a ataques DDoS o picos anómalos de tráfico.
- **Independencia Evolutiva:** Arquitectura Hexagonal que permite cambiar la base de datos o el bus de eventos en el futuro sin reescribir la lógica de negocio.

## 3. Alcance y Funcionalidades (Features)

### 3.1. Gestión de Catálogo (Backoffice)
- **Creación y Edición:** Dar de alta productos con SKU base, precio, categorías, etiquetas y atributos personalizados.
- **Variantes (Inventory):** Soporte para Sub-SKUs o variantes (ej. tallas, colores), cada una con su propio stock, ID numérico y precio opcional.
- **Imágenes:** Organización de imágenes asociadas al producto mediante URLs y un orden específico.
- **SEO Ready:** Generación automática de URLs amigables (slugs) a partir del nombre y SKU para indexación web.
- **Ciclo de Vida:** Los productos nacen en estado `DRAFT`, pasan a `ACTIVE` y pueden ser `ARCHIVED` (soft delete) para preservar integridad histórica en el Data Warehouse.

### 3.2. Operaciones de Tienda (Storefront)
- **Listado Rápido:** Retornar catálogo paginado o completo con la menor fricción posible.
- **Consulta por Slug:** Búsqueda por texto amigable (SEO) para las páginas de detalle de producto en el frontend (Next.js).
- **Control de Disponibilidad:** Exponer una consulta ligera de stock (`/availability`) para que el *Cart Service* bloquee intenciones de compra cuando no hay inventario físico.

## 4. Arquitectura de Software

- **Patrón:** Arquitectura Hexagonal (Ports and Adapters) / Clean Architecture.
- **Stack Tecnológico:**
  - **Lenguaje:** Java 25 (usando Records y Virtual Threads).
  - **Framework:** Spring Boot 3.5.3.
  - **Identificadores:** TSID (Time-Sorted Unique Identifiers) en lugar de UUIDs para mejorar el rendimiento de indexación en la base de datos.
- **Infraestructura y Persistencia:**
  - **Almacenamiento Primario:** MongoDB (NoSQL) orientado a documentos para modelar la entidad completa del Producto sin los costosos `JOINs` de bases de datos relacionales.
  - **Caché Distribuido:** Redis (Spring Cache). Lecturas cacheadas e invalidación reactiva inmediata tras actualizaciones de stock/precio.
  - **Bus de Eventos:** RabbitMQ (AMQP). Emisión de eventos de dominio (`ProductCreatedEvent`) para propagar cambios de estado a otros microservicios (ej. sincronizar un motor de búsqueda externo en el futuro).
   - **Observabilidad:** Micrometer + Prometheus para métricas, Zipkin para trazabilidad distribuida. Todos los microservicios comparten un stack unificado via el proyecto [`ecommerce-observability`](https://github.com/.../ecommerce-observability). Métricas custom de negocio (creación, stock, eventos) exportadas a Prometheus via endpoint `/management/prometheus`.

## 5. Contratos de API REST

| Método | Endpoint | Acción | Políticas de Resiliencia / Caché |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/products` | Listar catálogo | `@Cacheable`, `@RateLimiter` |
| `GET` | `/api/v1/products/{id}` | Obtener producto por ID | `@Cacheable`, `@RateLimiter` |
| `GET` | `/api/v1/products/slug/{slug}` | Obtener producto por SEO | `@Cacheable`, `@RateLimiter` |
| `GET` | `/api/v1/products/variants/{variantId}/availability`| Verificar stock (Cart) | `@Bulkhead`, No cacheado (Real-time) |
| `POST` | `/api/v1/products` | Crear nuevo producto | Invalida Caché (Evict) |
| `PUT` | `/api/v1/products/{id}` | Actualizar detalles | Invalida Caché (Evict) |
| `PUT` | `/api/v1/products/{id}/activate` | Activar producto | Invalida Caché (Evict) |
| `DELETE`| `/api/v1/products/{id}` | Archivar producto | Invalida Caché (Evict) |

## 6. Decisiones de Diseño Críticas (ADRs)
1. **CQRS Lógico:** Separación de interfaces de comandos (Escrituras) y consultas (Lecturas) en la capa de aplicación, preparándolo para un CQRS Estricto en el futuro si la carga lo amerita.
2. **Tell, Don't Ask:** Toda la manipulación de stock (aumentar/reducir) y estado está encapsulada como comportamiento dentro del agregado `Product` (Dominio Rico), rechazando el modelo anémico.
3. **Manejo de Errores Global:** Implementación de `@RestControllerAdvice` y RFC 7807 (Problem Details) para respuestas HTTP de error limpias y estandarizadas.
