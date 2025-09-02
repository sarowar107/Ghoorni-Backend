FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and configuration
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

# Install netcat for health checks (optional)
RUN apk add --no-cache netcat-openbsd

# Create app directory
WORKDIR /app

# Copy the built jar file
COPY --from=builder /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p resources/uploads

# Expose the port
EXPOSE 8080

# Run the application directly with java -jar
CMD ["java", "-jar", "app.jar"]