FROM maven:3.9.4-openjdk-21-slim AS build

WORKDIR /build

# Copy parent pom and module pom
COPY pom.xml .
COPY ingestion-pipeline/pom.xml ./ingestion-pipeline/

# Copy source code
COPY ingestion-pipeline/src ./ingestion-pipeline/src

# Build the ingestion-pipeline module
RUN mvn clean package -pl ingestion-pipeline -DskipTests

FROM openjdk:21-jre-slim

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /build/ingestion-pipeline/target/*.jar app.jar

EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8084/api/ingestion-pipeline/health || exit 1

CMD ["java", "-jar", "app.jar"]


