# Multi-stage build for Spring Boot application

# Stage 1: Build stage
FROM openjdk:17-jdk-slim AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM openjdk:17-jre-slim AS production

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN groupadd -r ubuzima && useradd -r -g ubuzima ubuzima

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R ubuzima:ubuzima /app

# Switch to non-root user
USER ubuzima

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="Ubuzima Development Team <dev@ubuzima.rw>"
LABEL version="1.0.0"
LABEL description="Ubuzima Family Planning Backend API"
