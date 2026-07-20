# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy the maven executable and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Grant execute permission for the mvnw script
RUN chmod +x mvnw

# Resolve dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy the source code and build the application
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the jar and entrypoint script from the build stage
COPY --from=build /app/target/*.jar app.jar
COPY entrypoint.sh .

# Grant execute permission for the script
USER root
RUN chmod +x entrypoint.sh
USER spring:spring

# Expose the application port
EXPOSE 8080

# Configure environment variables for PostgreSQL (can be overridden at runtime)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/foodfiesta
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=password

# Execution script
ENTRYPOINT ["./entrypoint.sh"]
