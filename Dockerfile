# Build stage: compile and package the Spring Boot jar
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
# Tests and lint run in CI; the image build only compiles and packages
RUN ./mvnw -B -q -DskipTests -Dcheckstyle.skip -Djacoco.skip package

# Run stage: only the JRE and the jar
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/sports-booking-1.0.0.jar app.jar
# Docker Compose support is for running from source, not inside the container
ENV SPRING_DOCKER_COMPOSE_ENABLED=false
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
