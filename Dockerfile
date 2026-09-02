# --- Build stage ---
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /build

# Cache dependencies first
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S siakad && adduser -S siakad -G siakad
COPY --from=build /build/target/siakad-be-*.jar app.jar
RUN chown siakad:siakad app.jar
USER siakad

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -q -O- http://localhost:${SERVER_PORT:-8080}/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
