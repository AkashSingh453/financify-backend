# Dockerfile (Multi-stage Version)

# Stage 1: Build
FROM gradle:8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the result from Stage 1
COPY --from=build /home/gradle/src/build/libs/*all.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]