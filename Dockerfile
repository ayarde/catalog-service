# --- STAGE 1: Build ---
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

# Copiamos solo los archivos de configuración de Gradle para aprovechar la caché de capas
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Descargamos dependencias (esto fallará si no hay código, pero ayuda a la caché en cambios posteriores)
RUN ./gradlew dependencies --no-daemon || true

# Copiamos el código fuente de todos los módulos
COPY domain/ domain/
COPY application/ application/
COPY infrastructure/ infrastructure/
COPY boot/ boot/

# Compilamos y generamos el Fat JAR (omitimos tests para acelerar el build del contenedor)
RUN ./gradlew :boot:bootJar --no-daemon -x test

# --- STAGE 2: Run ---
FROM eclipse-temurin:25-jre-alpine

# Instalamos curl para el HealthCheck de Docker si fuera necesario
RUN apk add --no-cache curl

# Creamos un usuario no-root por seguridad
RUN addgroup -S ecommerce && adduser -S catalog -G ecommerce
USER catalog

WORKDIR /app

# Copiamos el JAR generado desde la etapa anterior
# Usamos el wildcard para que sea independiente de la versión
COPY --from=builder /build/boot/build/libs/catalog-service-*.jar app.jar

# Exponemos el puerto definido en la arquitectura
EXPOSE 8081

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Comando de arranque
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
