# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Run ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create uploads directory and non-root user
RUN mkdir -p /app/uploads && \
    addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app

USER spring:spring

# Copy fat JAR from build stage
COPY --from=build /app/target/doatec-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
