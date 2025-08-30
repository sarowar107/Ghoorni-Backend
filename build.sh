#!/usr/bin/env bash
# Exit on error
set -e

# Make mvnw executable
chmod +x mvnw

# Build the application using Maven
./mvnw clean package -DskipTests
