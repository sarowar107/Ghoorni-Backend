FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
