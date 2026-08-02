# backend/Dockerfile

# ─── STAGE 1: Build ─────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first — Docker caches this layer if pom.xml didn't change
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# ─── STAGE 2: Run ────────────────────────────────────────────────────────
# We use a slim JRE (not full JDK) since we only need to RUN the jar, not compile it
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
