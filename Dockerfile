# ---- Build stage ----
# Builds the jar inside the image itself, so `docker build .` alone is
# reproducible - it no longer depends on a jar someone already built on the
# host with `mvnw package` first.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy just the wrapper and pom first so dependency resolution is cached by
# Docker's layer cache and only re-runs when pom.xml actually changes.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw package -DskipTests -B

# ---- Runtime stage ----
# JRE, not JDK - the running app never compiles anything, and the JRE image
# is both smaller and has a smaller attack surface.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as a dedicated, unprivileged user instead of the image's default root.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/task_of_cravita-0.0.1-SNAPSHOT.jar app.jar

# Documents the default; the actual bind port always comes from the required
# PORT env var at runtime (application-prod.properties has no fallback for it).
EXPOSE 8080

# Hits the unauthenticated /actuator/health endpoint so an orchestrator (or
# `docker ps`) can tell "container running" apart from "app actually up."
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider "http://localhost:${PORT:-8080}/actuator/health" || exit 1

ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=prod"]
