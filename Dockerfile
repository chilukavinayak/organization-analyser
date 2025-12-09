# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/organization-analyzer-1.0.0.jar app.jar

# Copy sample data
COPY --from=build /app/src/main/resources/employees.csv /app/data/employees.csv

# Create directory for user data
RUN mkdir -p /app/data

ENTRYPOINT ["java", "-jar", "app.jar"]

# Default to sample data if no file provided
CMD ["/app/data/employees.csv"]
