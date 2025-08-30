FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
COPY entrypoint.sh .

# Use dos2unix to convert line endings from CRLF to LF
RUN apk add --no-cache dos2unix
RUN dos2unix entrypoint.sh

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]