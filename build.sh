#!/usr/bin/env bash
# Exit on error
set -e

# Build the application using Maven
./mvnw clean package -DskipTests
