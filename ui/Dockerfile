FROM maven:3.9.4-openjdk-21-slim AS build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
COPY --from=build target/*.jar app.jar
EXPOSE 8084
CMD ["java", "-jar", "app.jar"]
