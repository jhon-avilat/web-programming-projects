# Stage 1: build the JAR with Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY . .
RUN mvn -q -DskipTests package

# Stage 2: run it on a small Java runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
ENTRYPOINT ["java", "-Xmx300m", "-jar", "app.jar"]
