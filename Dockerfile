# Multi-stage build for optimized production image
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Install necessary packages for building
RUN apk add --no-cache curl

# Copy Maven wrapper and configuration files first (for better caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application with optimizations
RUN ./mvnw clean package -DskipTests -B -q && \
    java -Djarmode=layertools -jar target/*.jar list

# Production stage
FROM eclipse-temurin:17-jre-alpine

# Add labels for better container management
LABEL maintainer="ghoorni-team" \
    version="1.0" \
    description="Ghoorni Backend Application"

# Install necessary packages
RUN apk add --no-cache \
    netcat-openbsd \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# Set timezone
ENV TZ=Asia/Dhaka
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create non-root user for security
RUN addgroup -g 1001 -S ghoorni && \
    adduser -S ghoorni -u 1001 -G ghoorni

# Create app directory and set ownership
WORKDIR /app
RUN chown -R ghoorni:ghoorni /app

# Create necessary directories with proper permissions
RUN mkdir -p resources/uploads logs && \
    chown -R ghoorni:ghoorni resources logs

# Copy the built jar file with proper ownership
COPY --from=builder --chown=ghoorni:ghoorni /app/target/*.jar app.jar

# Switch to non-root user
USER ghoorni

# Set JVM options for production
ENV JAVA_OPTS="-Xms256m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=production"

# Set Spring Boot specific environment variables
ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=production

# Expose the port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || nc -z localhost 8080 || exit 1

# Create startup script for better signal handling
RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'exec java $JAVA_OPTS -jar app.jar "$@"' >> /app/start.sh && \
    chmod +x /app/start.sh

# Use startup script as entrypoint
ENTRYPOINT ["/app/start.sh"]

# Default command (can be overridden)
CMD []

# Add volume for persistent data
VOLUME ["/app/resources/uploads", "/app/logs"]