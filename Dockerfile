# Multi-stage build for Smart File Management System

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-25 AS builder

LABEL maintainer="Smart File Management"

WORKDIR /build

# Copy pom.xml
COPY pom.xml .

# Copy source code
COPY src ./src
COPY frontend ./frontend

# Build the project
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:25-jre-alpine

LABEL maintainer="Smart File Management"
LABEL description="Smart File Management System - A modern full-stack file management application"

WORKDIR /app

# Create uploads directory
RUN mkdir -p uploads

# Copy JAR from builder stage
COPY --from=builder /build/target/smart-file-management.jar .

# Expose port
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xmx512m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080 || exit 1

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar smart-file-management.jar"]
