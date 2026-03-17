FROM maven:3.9.4-openjdk-21-slim AS build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:21-jre-slim
COPY --from=build target/*.jar app.jar
EXPOSE 8083
CMD ["java", "-jar", "app.jar"]
